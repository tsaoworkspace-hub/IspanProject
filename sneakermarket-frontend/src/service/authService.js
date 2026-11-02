import { getCsrfToken, resetCsrfToken } from "./csrfService";

const API_BASE_URL = (
  import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api"
).replace(/\/$/, "");

const AUTH_API_BASE_URL = `${API_BASE_URL}/auth`;

const MEMBER_API_BASE_URL = `${API_BASE_URL}/members`;

async function withCsrfHeader(headers = {}) {
  const csrf = await getCsrfToken();
  return {
    ...headers,
    [csrf.headerName]: csrf.token,
  };
}


async function readErrorResponse(response, defaultMessage) {
  let data = null;

  try {
    data = await response.clone().json();
  } catch {
    try {
      data = await response.text();
    } catch {
      data = null;
    }
  }

  const error = new Error(
    data?.message || (typeof data === "string" && data) || defaultMessage,
  );

  error.status = response.status;

  error.data = data;

  throw error;
}

// =====================================================
// 登入
//
// deleted_at 判斷由後端負責。
// =====================================================
export async function login(data) {
  const response = await fetch(
    `${AUTH_API_BASE_URL}/login`,

    {
      method: "POST",

      headers: await withCsrfHeader({
        "Content-Type": "application/json",
      }),

      credentials: "include",

      body: JSON.stringify({
        account: data.account,

        password: data.password,
      }),
    },
  );

  if (!response.ok) {
    await readErrorResponse(response, "登入失敗");
  }

  return await response.json();
}

// =====================================================
// 註冊
//
// 已刪除帳號是否能重新註冊
// 由後端判斷。
// =====================================================
export async function register(data) {
  const response = await fetch(
    MEMBER_API_BASE_URL,

    {
      method: "POST",

      headers: await withCsrfHeader({
        "Content-Type": "application/json",
      }),

      credentials: "include",

      body: JSON.stringify({
        account: data.account,

        password: data.password,

        name: data.name,

        email: data.email,

        phone: data.phone,

        address: data.address,
      }),
    },
  );

  if (!response.ok) {
    await readErrorResponse(response, "註冊失敗");
  }

  return await response.json();
}

// =====================================================
// 目前登入會員
//
// deleted_at != NULL 時
// 後端會回 401。
// =====================================================
export async function getCurrentMember() {
  const response = await fetch(
    `${AUTH_API_BASE_URL}/me`,

    {
      method: "GET",

      credentials: "include",
    },
  );

  if (!response.ok) {
    await readErrorResponse(response, "尚未登入");
  }

  return await response.json();
}

// =====================================================
// 登出
// =====================================================
export async function logout() {
  const response = await fetch(
    `${AUTH_API_BASE_URL}/logout`,

    {
      method: "POST",

      headers: await withCsrfHeader(),

      credentials: "include",
    },
  );

  if (!response.ok) {
    await readErrorResponse(response, "登出失敗");
  }

  resetCsrfToken();
}
