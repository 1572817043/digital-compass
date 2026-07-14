export type UserRole = 'USER' | 'ADMIN';

export interface LoginUser {
  id: number;
  username: string;
  nickname: string;
  role: UserRole;
}

export interface LoginResponse {
  token: string;
  tokenType: 'Bearer';
  expiresIn: number;
  user: LoginUser;
}

export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}
