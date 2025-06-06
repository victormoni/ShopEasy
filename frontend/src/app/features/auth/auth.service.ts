import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import {
  Observable,
  BehaviorSubject,
  of,
  throwError,
  switchMap,
  firstValueFrom,
} from 'rxjs';
import { catchError, mapTo, tap } from 'rxjs/operators';
import { JwtHelperService } from '@auth0/angular-jwt'; // npm install @auth0/angular-jwt

interface AuthResponse {
  accessToken: string;
  refreshToken: string;
}

export interface UserResponse {
  id: number;
  username: string;
  role: string;
}

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private apiUrl = environment.apiUrl;
  private loggedInSubject = new BehaviorSubject<boolean>(!!this.getToken());
  public isLoggedIn$ = this.loggedInSubject.asObservable();

  // Guardamos o role (string) para não precisar chamar /users/me toda hora
  private isAdminSubject = new BehaviorSubject<boolean>(false);
  public isAdmin$ = this.isAdminSubject.asObservable();

  constructor(private http: HttpClient, private jwtHelper: JwtHelperService) {
    this.loadUserFromToken();
  }

  private loadUserFromToken(): void {
    const token = this.getToken();
    if (token && !this.jwtHelper.isTokenExpired(token)) {
      const decoded: any = this.jwtHelper.decodeToken(token);

      // 1) Pode existir "decoded.role" (uma string única), ex: "ADMIN"
      const singleRole: string | null = decoded?.role ?? null;

      // 2) Pode existir "decoded.roles" como array de strings, ex: ["ROLE_ADMIN", "ROLE_USER"]
      // Ou até ["ADMIN","USER"], dependendo de como você gerou no backend.
      const rolesArray: string[] = Array.isArray(decoded?.roles)
        ? decoded.roles
        : [];

      // 3) Se houver singleRole (claim "role": "ADMIN"), basta comparar diretamente:
      if (singleRole) {
        this.isAdminSubject.next(singleRole === 'ADMIN');
      }
      // 4) Senão, se vier um array de roles, precisamos verificar adequadamente:
      else if (rolesArray.length > 0) {
        // ★ Ajuste conforme o formato que seu backend realmente envia:
        //
        // Se o backend envia ["ROLE_ADMIN", "ROLE_USER"], use:
        const isAdminViaRole = rolesArray.includes('ROLE_ADMIN'); // compara exato "ROLE_ADMIN"
        //
        // Caso seu backend envie ["ADMIN","USER"], use:
        // const isAdminViaRole = rolesArray.includes('ADMIN');
        //
        // Ou, se quiser aceitar ambos os formatos (com ou sem "ROLE_"), pode fazer:
        // const isAdminViaRole = rolesArray.some(r => r === 'ADMIN' || r === 'ROLE_ADMIN');

        this.isAdminSubject.next(isAdminViaRole);
      }
      // 5) Se não veio claim alguma de roles, então não é admin:
      else {
        this.isAdminSubject.next(false);
      }

      // 6) Por fim, sinaliza que o usuário está logado
      this.loggedInSubject.next(true);
    } else {
      // Token ausente ou expirado → limpar dados e emitir "não logado"
      this.clearAuthData();
    }
  }

  private clearAuthData(): void {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    this.loggedInSubject.next(false);
    this.isAdminSubject.next(false);
  }

  login(username: string, password: string): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.apiUrl}/api/auth/login`, {
        username,
        password,
      })
      .pipe(
        tap((res) => {
          localStorage.setItem('accessToken', res.accessToken);
          localStorage.setItem('refreshToken', res.refreshToken);
          this.loggedInSubject.next(true);

          const decoded: any = this.jwtHelper.decodeToken(res.accessToken);
          const singleRole: string | null = decoded?.role ?? null;
          const rolesArray: string[] = Array.isArray(decoded?.roles)
            ? decoded.roles
            : [];

          if (singleRole) {
            this.isAdminSubject.next(singleRole === 'ADMIN');
          } else if (rolesArray.length > 0) {
            const isAdminViaRole = rolesArray.includes('ROLE_ADMIN');
            this.isAdminSubject.next(isAdminViaRole);
          } else {
            this.isAdminSubject.next(false);
          }
        }),
        catchError((err) => {
          return throwError(() => new Error('Erro no login'));
        })
      );
  }

  register(username: string, password: string, role: string): Observable<void> {
    return this.http
      .post(`${this.apiUrl}/api/auth/register`, {
        username,
        password,
        role,
      })
      .pipe(
        catchError((err: HttpErrorResponse) => {
          if (err.status === 409) {
            return throwError(() => new Error('Usuário já existe'));
          }
          return throwError(() => new Error('Erro ao registrar usuário'));
        }),
        switchMap(() => of(void 0))
      );
  }

  logout(): void {
    this.clearAuthData();
    this.loggedInSubject.next(false);
    this.isAdminSubject.next(false);
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
  }

  getToken(): string | null {
    return localStorage.getItem('accessToken');
  }

  getRefreshToken(): string | null {
    return localStorage.getItem('refreshToken');
  }

  async refreshToken(): Promise<string> {
    const refreshToken = this.getRefreshToken();
    if (!refreshToken) {
      this.clearAuthData();
      return Promise.reject('Sem refresh token');
    }

    try {
      // Usamos firstValueFrom para converter o Observable em Promise<string>
      const res = await firstValueFrom(
        this.http.post<AuthResponse>(`${this.apiUrl}/api/auth/refresh`, {
          refreshToken,
        })
      );

      // Se chegamos aqui, recebemos novos tokens
      localStorage.setItem('accessToken', res.accessToken);
      localStorage.setItem('refreshToken', res.refreshToken);
      this.loggedInSubject.next(true);

      const decoded: any = this.jwtHelper.decodeToken(res.accessToken);
      const role = decoded?.role || decoded?.roles || null;
      this.isAdminSubject.next(role === 'ADMIN');

      return res.accessToken;
    } catch (err) {
      // Se ocorrer erro (status 401 ou outro), limpamos tudo e rejeitamos
      this.clearAuthData();
      return Promise.reject('Não foi possível renovar token');
    }
  }
}
