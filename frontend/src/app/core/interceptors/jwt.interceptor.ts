import { Injectable } from '@angular/core';
import {
  HttpInterceptor,
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpErrorResponse,
} from '@angular/common/http';
import { Observable, throwError, from } from 'rxjs';
import { catchError, switchMap } from 'rxjs/operators';
import { AuthService } from '../../features/auth/auth.service';

@Injectable()
export class JwtInterceptor implements HttpInterceptor {
  constructor(private authService: AuthService) {}

  intercept(
    req: HttpRequest<any>,
    next: HttpHandler
  ): Observable<HttpEvent<any>> {
    // Não adiciona token para endpoints de autenticação
    if (
      req.url.endsWith('/api/auth/register') ||
      req.url.endsWith('/api/auth/login') ||
      req.url.endsWith('/api/auth/refresh')
    ) {
      return next.handle(req);
    }

    const token = this.authService.getToken();
    let authReq = req;

    if (token) {
      authReq = req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`,
        },
      });
    }

    return next.handle(authReq).pipe(
      catchError((err) => {
        // Se receber 401, tenta refresh token uma vez
        if (err instanceof HttpErrorResponse && err.status === 401) {
          // Chama método para refresh do token
          return from(this.authService.refreshToken()).pipe(
            switchMap((newToken) => {
              // Se refresh bem-sucedido, clona a requisição original com novo token
              const retryReq = req.clone({
                setHeaders: {
                  Authorization: `Bearer ${newToken}`,
                },
              });
              return next.handle(retryReq);
            }),
            catchError((refreshErr) => {
              // Se também falhar no refresh, força logout
              this.authService.logout();
              return throwError(() => refreshErr);
            })
          );
        }
        return throwError(() => err);
      })
    );
  }
}
