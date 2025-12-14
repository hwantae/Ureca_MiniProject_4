function getCsrfTokenFromCookie() {
    const match = document.cookie.match(/XSRF-TOKEN=([^;]+)/);
    return match ? decodeURIComponent(match[1]) : null;
}

async function csrfFetch(url, options = {}) {
    const csrfToken = getCsrfTokenFromCookie();

    // 기본 옵션
    const defaultOptions = {
        credentials: 'same-origin',
        headers: {}
    };

    // 사용자 옵션 병합
    const mergedOptions = {
        ...defaultOptions,
        ...options,
        headers: {
            ...defaultOptions.headers,
            ...(options.headers || {})
        }
    };

    // ★ GET 요청에는 CSRF 헤더를 안 붙여도 되지만 붙여도 안전함
    if (csrfToken) {
        mergedOptions.headers['X-XSRF-TOKEN'] = csrfToken;
    }

    return fetch(url, mergedOptions);
}


// logout
// common.js

async function csrfLogout() {
    try {
        let response = await csrfFetch('/logout', {
            method: 'POST'
        });

        // 1. fetch는 기본적으로 302 리다이렉트를 자동으로 따라갑니다.
        // 따라서 response.redirected가 true이거나,
        // 최종 도착한 url에 'login'이 포함되어 있으면 로그아웃 성공으로 간주합니다.
        if (response.redirected || response.url.includes('login')) {
            window.location.href = '/login'; // 로그인 페이지로 이동
            return;
        }

        // 2. 만약 서버가 리다이렉트 없이 JSON을 주는 설정을 했다면 여기서 처리
        let data = await response.json();

        if(data.result == "success"){
            window.location.href = '/login';
        } else {
            alert('로그아웃에 실패했습니다.');
        }
    } catch (error) {
        console.error("Logout Error:", error);
        // 에러가 나더라도 토큰을 지우고 로그인 페이지로 보내는 것이 안전함
        window.location.href = '/login';
    }
}
