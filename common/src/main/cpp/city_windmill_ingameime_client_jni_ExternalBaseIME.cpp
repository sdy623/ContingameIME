#include <Windows.h>
#include <cstdarg>
#include <cstdio>
#include <string>
#include <vector>
#include "../../../../3rd/IngameIME-Common/include/IngameIME.hpp"
#include "city_windmill_ingameime_client_jni_ExternalBaseIME.h"

#define GLOBAL(x) env->NewGlobalRef(x)
#define FIELD(clazz, fieldName, type) env->GetObjectField(clazz, env->GetFieldID(clazz, fieldName, type))
#define STATICFIELD(clazz, fieldName, type) env->GetStaticObjectField(clazz, env->GetStaticFieldID(clazz, fieldName, type))
#define FREEGLOBAL(x) if(x){ env->DeleteGlobalRef(x); x = NULL; }

IngameIME::InputContext* api = nullptr;
HWND g_hwnd = NULL;
bool g_fullscreen = false;

JavaVM* g_vm;
jobject go_ExternalBaseIME = NULL;
//CallbackMethods
jmethodID gmtd_onCandidateList = NULL;
jmethodID gmtd_onComposition = NULL;
jmethodID gmtd_onGetCompExt = NULL;
jmethodID gmtd_onAlphaMode = NULL;
jmethodID gmtd_onInputLanguage = NULL;
//CompositionState
jobject go_CompositionState_START = NULL;
jobject go_CompositionState_UPDATE = NULL;
jobject go_CompositionState_END = NULL;
jobject go_CompositionState_COMMIT = NULL;

enum InputLanguageCode
{
    INPUT_LANG_OTHER = 0,
    INPUT_LANG_CHINESE = 1,
    INPUT_LANG_JAPANESE = 2,
    INPUT_LANG_ENGLISH = 3,
};

static void dbg_out(const char* fmt, ...)
{
    char buf[2048];
    DWORD tid = GetCurrentThreadId();
    DWORD ms = GetTickCount();
    int prefixLen = _snprintf_s(buf, sizeof(buf), _TRUNCATE, "[IngameIME-JNI][%lu][%lu] ", (unsigned long)ms, (unsigned long)tid);
    if (prefixLen < 0) prefixLen = 0;

    va_list args;
    va_start(args, fmt);
    _vsnprintf_s(buf + prefixLen, sizeof(buf) - (size_t)prefixLen, _TRUNCATE, fmt, args);
    va_end(args);

    OutputDebugStringA(buf);
    OutputDebugStringA("\n");
}

static jint get_input_language_code()
{
    HKL hkl = GetKeyboardLayout(0);
    LANGID langId = LOWORD(hkl);
    switch (PRIMARYLANGID(langId))
    {
    case LANG_CHINESE:
        return INPUT_LANG_CHINESE;
    case LANG_JAPANESE:
        return INPUT_LANG_JAPANESE;
    case LANG_ENGLISH:
        return INPUT_LANG_ENGLISH;
    default:
        return INPUT_LANG_OTHER;
    }
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* jvm, void* reserved) {
    jint result = -1;
    JNIEnv* env;
    if (jvm->GetEnv((void**)&env, JNI_VERSION_1_8) != JNI_OK) {
        return -1;
    }

    g_vm = jvm;

    result = JNI_VERSION_1_8;
    return result;
}

JNIEXPORT void JNICALL JNI_OnUnload(JavaVM* vm, void* reserved)
{
    Java_city_windmill_ingameime_client_jni_ExternalBaseIME_nUninitialize(NULL, NULL);
}

template<typename F>
HRESULT call_with_env(F const& pf) {
    if (g_vm == NULL) return E_FAIL;

    JNIEnv* env;
    jint status = g_vm->GetEnv((void**)&env, JNI_VERSION_1_8);

    if (status == JNI_EDETACHED || env == NULL) {
        status = g_vm->AttachCurrentThread((void**)&env, NULL);
        if (status == JNI_OK) {
            pf(env);
            g_vm->DetachCurrentThread();
            return S_OK;
        }
        return status;
    }
    pf(env);
    return S_OK;
}

// Helpers to post callbacks into JVM

void post_onCandidateList(const IngameIME::CandidateListContext* ctx)
{
    dbg_out("post_onCandidateList ctx=%p count=%zu sel=%d", ctx, ctx ? ctx->candidates.size() : 0, ctx ? ctx->selection : -1);
    call_with_env([ctx](JNIEnv* env){
        jobjectArray cand = NULL;
        if (ctx && !ctx->candidates.empty()) {
            jclass clString = env->FindClass("java/lang/String");
            cand = env->NewObjectArray((jsize)ctx->candidates.size(), clString, NULL);
            int i = 0;
            for (auto &s : ctx->candidates) {
                auto str = env->NewStringUTF(s.c_str());
                env->SetObjectArrayElement(cand, i++, str);
            }
        }
        jint sel = ctx ? ctx->selection : -1;
        env->CallVoidMethod(go_ExternalBaseIME, gmtd_onCandidateList, cand, sel);
    });
}

void post_onComposition(IngameIME::CompositionState state, const IngameIME::PreEditContext* ctx)
{
    dbg_out("post_onComposition state=%d ctx=%p caret=%d len=%zu", (int)state, ctx, ctx ? (int)ctx->selStart : -1, ctx ? ctx->content.size() : 0);
    call_with_env([state, ctx](JNIEnv* env){
        jobject jstate = nullptr;
        jstring str = nullptr;
        jint caret = 0;
        switch (state) {
        case IngameIME::CompositionState::Begin:
            jstate = go_CompositionState_START;
            break;
        case IngameIME::CompositionState::End:
            jstate = go_CompositionState_END;
            break;
        case IngameIME::CompositionState::Update:
            jstate = go_CompositionState_UPDATE;
            if (ctx) {
                caret = ctx->selStart;
                if (!ctx->content.empty()) str = env->NewStringUTF(ctx->content.c_str());
            }
            break;
        default:
            break;
        }
        env->CallVoidMethod(go_ExternalBaseIME, gmtd_onComposition, str, caret, jstate);
    });
}

void post_onCommit(const std::string &commit)
{
    dbg_out("post_onCommit len=%zu", commit.size());
    call_with_env([&commit](JNIEnv* env){
        jstring str = env->NewStringUTF(commit.c_str());
        env->CallVoidMethod(go_ExternalBaseIME, gmtd_onComposition, str, (jint)0, go_CompositionState_COMMIT);
    });
}

void post_onInputMode(IngameIME::InputMode mode)
{
    dbg_out("post_onInputMode mode=%d", (int)mode);
    call_with_env([mode](JNIEnv* env){
        jboolean isAlpha = (mode == IngameIME::InputMode::AlphaNumeric);
        env->CallVoidMethod(go_ExternalBaseIME, gmtd_onAlphaMode, isAlpha);
        if (gmtd_onInputLanguage) env->CallVoidMethod(go_ExternalBaseIME, gmtd_onInputLanguage, get_input_language_code());
    });
}

// Candidate list callback
static std::function<void(const IngameIME::CandidateListState, const IngameIME::CandidateListContext*)> candidateCb = nullptr;
// Preedit callback
static std::function<void(const IngameIME::CompositionState, const IngameIME::PreEditContext*)> preeditCb = nullptr;
// Commit callback
static std::function<void(const std::string)> commitCb = nullptr;
// Input mode callback
static std::function<void(const IngameIME::InputMode)> inputModeCb = nullptr;

JNIEXPORT void JNICALL Java_city_windmill_ingameime_client_jni_ExternalBaseIME_nInitialize(JNIEnv* env, jobject obj, jlong hwnd)
{
    if (api) return; // already initialized

    g_hwnd = (HWND)hwnd;
    dbg_out("nInitialize hwnd=0x%p g_fullscreen=%d", g_hwnd, (int)g_fullscreen);

    go_ExternalBaseIME = GLOBAL(obj);
    // CompositionState enums
    auto clStateName = "city/windmill/ingameime/client/jni/ExternalBaseIME$CompositionState";
    auto clStateType = "Lcity/windmill/ingameime/client/jni/ExternalBaseIME$CompositionState;";
    jclass clState = env->FindClass(clStateName);
    go_CompositionState_START = GLOBAL(STATICFIELD(clState, "Start", clStateType));
    go_CompositionState_UPDATE = GLOBAL(STATICFIELD(clState, "Update", clStateType));
    go_CompositionState_END = GLOBAL(STATICFIELD(clState, "End", clStateType));
    go_CompositionState_COMMIT = GLOBAL(STATICFIELD(clState, "Commit", clStateType));
    // Callbacks
    jclass clBaseIME = env->GetObjectClass(obj);
    gmtd_onCandidateList = env->GetMethodID(clBaseIME, "onCandidateList", "([Ljava/lang/String;I)V");
    gmtd_onComposition = env->GetMethodID(clBaseIME, "onComposition", "(Ljava/lang/String;ILcity/windmill/ingameime/client/jni/ExternalBaseIME$CompositionState;)V");
    gmtd_onGetCompExt = env->GetMethodID(clBaseIME, "onGetCompExt", "()[I");
    gmtd_onAlphaMode = env->GetMethodID(clBaseIME, "onAlphaMode", "(Z)V");
    gmtd_onInputLanguage = env->GetMethodID(clBaseIME, "onInputLanguage", "(I)V");

    // Create TSF InputContext
    api = IngameIME::CreateInputContextWin32(g_hwnd, IngameIME::API::TextServiceFramework, g_fullscreen);
    dbg_out("CreateInputContextWin32 api=%p", api);

    // Register callbacks
    preeditCb = [](const IngameIME::CompositionState state, const IngameIME::PreEditContext* ctx) { post_onComposition(state, ctx); };
    api->IngameIME::PreEditCallbackHolder::setCallback(preeditCb);

    commitCb = [](const std::string commit) { post_onCommit(commit); };
    api->IngameIME::CommitCallbackHolder::setCallback(commitCb);

    candidateCb = [](const IngameIME::CandidateListState s, const IngameIME::CandidateListContext* ctx) { post_onCandidateList(ctx); };
    api->IngameIME::CandidateListCallbackHolder::setCallback(candidateCb);

    inputModeCb = [](const IngameIME::InputMode m) { post_onInputMode(m); };
    api->IngameIME::InputModeCallbackHolder::setCallback(inputModeCb);

    // Notify initial input language
    if (gmtd_onInputLanguage) {
        env->CallVoidMethod(go_ExternalBaseIME, gmtd_onInputLanguage, get_input_language_code());
    }
}

JNIEXPORT void JNICALL Java_city_windmill_ingameime_client_jni_ExternalBaseIME_nUninitialize(JNIEnv* env, jobject)
{
    if (api) {
        delete api;
        api = nullptr;
    }
    FREEGLOBAL(go_ExternalBaseIME);
    FREEGLOBAL(go_CompositionState_START);
    FREEGLOBAL(go_CompositionState_UPDATE);
    FREEGLOBAL(go_CompositionState_END);
    FREEGLOBAL(go_CompositionState_COMMIT);
}

JNIEXPORT void JNICALL Java_city_windmill_ingameime_client_jni_ExternalBaseIME_nSetState(JNIEnv*, jobject, jboolean state)
{
    dbg_out("nSetState(%d) api=%p", (int)state, api);
    if (api) api->setActivated(state);
}

JNIEXPORT void JNICALL Java_city_windmill_ingameime_client_jni_ExternalBaseIME_nSetFullScreen(JNIEnv*, jobject, jboolean fullscreen)
{
    // Recreate InputContext with new uiLess setting
    g_fullscreen = (bool)fullscreen;
    dbg_out("nSetFullScreen(%d) hwnd=0x%p api=%p", (int)fullscreen, g_hwnd, api);
    if (api) {
        delete api;
        api = IngameIME::CreateInputContextWin32(g_hwnd, IngameIME::API::TextServiceFramework, g_fullscreen);
        dbg_out("Recreated api=%p", api);
        // re-register callbacks
        api->IngameIME::PreEditCallbackHolder::setCallback(preeditCb);
        api->IngameIME::CommitCallbackHolder::setCallback(commitCb);
        api->IngameIME::CandidateListCallbackHolder::setCallback(candidateCb);
        api->IngameIME::InputModeCallbackHolder::setCallback(inputModeCb);
        // notify input language
        call_with_env([](JNIEnv* env){ if (gmtd_onInputLanguage) env->CallVoidMethod(go_ExternalBaseIME, gmtd_onInputLanguage, get_input_language_code()); });
    }
}

JNIEXPORT void JNICALL Java_city_windmill_ingameime_client_jni_ExternalBaseIME_nSetPreEditRect(JNIEnv* env, jobject, jintArray rect)
{
    if (!api) return;
    jint buf[4] = {0,0,0,0};
    if (rect && env->GetArrayLength(rect) >= 4) {
        env->GetIntArrayRegion(rect, 0, 4, buf);
    }
    IngameIME::PreEditRect r;
    r.x = buf[0]; r.y = buf[1]; r.width = buf[2] - buf[0]; r.height = buf[3] - buf[1];

    // Throttle logs to avoid spam.
    static DWORD lastLogAt = 0;
    static jint lastBuf[4] = {0,0,0,0};
    DWORD now = GetTickCount();
    bool changed = (buf[0] != lastBuf[0] || buf[1] != lastBuf[1] || buf[2] != lastBuf[2] || buf[3] != lastBuf[3]);
    if (changed || (now - lastLogAt) > 500) {
        lastLogAt = now;
        lastBuf[0] = buf[0]; lastBuf[1] = buf[1]; lastBuf[2] = buf[2]; lastBuf[3] = buf[3];
        dbg_out("nSetPreEditRect [%d,%d,%d,%d]", (int)buf[0], (int)buf[1], (int)buf[2], (int)buf[3]);
    }

    api->setPreEditRect(r);
}
