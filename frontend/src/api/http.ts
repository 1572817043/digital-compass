import axios from 'axios';

declare module 'axios' {
  interface AxiosRequestConfig {
    skipAuthRedirect?: boolean;
  }
}

// 简单的全局事件系统，用于拦截器通知 Toast
type ToastHandler = (message: string, type: 'success' | 'error' | 'info') => void;
let toastHandler: ToastHandler | null = null;

export function registerToastHandler(handler: ToastHandler) {
  toastHandler = handler;
}

function showToast(message: string, type: 'success' | 'error' | 'info' = 'info') {
  toastHandler?.(message, type);
}

export const http = axios.create({
  baseURL: '/api',
  timeout: 15000,
});

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('digicompass_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

http.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status;
    const skipAuthRedirect = Boolean(error.config?.skipAuthRedirect);

    if (status === 401 && !skipAuthRedirect) {
      localStorage.removeItem('digicompass_token');
      localStorage.removeItem('digicompass_user');
      showToast('登录已过期，请重新登录', 'error');
      if (!window.location.pathname.startsWith('/login')) {
        window.location.href = '/login';
      }
    } else if (status === 403) {
      showToast('没有权限访问该功能', 'error');
    } else if (status === 404) {
      showToast('请求的资源不存在', 'error');
    } else if (status === 400) {
      const msg = error.response?.data?.message;
      if (msg) showToast(msg, 'error');
    } else if (status >= 500) {
      showToast('服务器异常，请稍后再试', 'error');
    } else if (!error.response) {
      showToast('网络连接异常，请稍后重试', 'error');
    }

    return Promise.reject(error);
  },
);
