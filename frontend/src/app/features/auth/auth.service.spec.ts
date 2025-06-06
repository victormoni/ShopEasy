import { TestBed, fakeAsync, tick } from '@angular/core/testing';
import { AuthService } from './auth.service';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { JwtHelperService } from '@auth0/angular-jwt';
import { of, throwError } from 'rxjs';

describe('AuthService', () => {
  let service: AuthService;
  let httpSpy: jasmine.SpyObj<HttpClient>;
  let jwtSpy: jasmine.SpyObj<JwtHelperService>;

  const dummyAuthResponse = {
    accessToken: 'valid.token.here',
    refreshToken: 'refresh.token.here',
  };

  const dummyDecodedToken = {
    role: 'ADMIN',
    roles: ['ROLE_ADMIN', 'ROLE_USER'],
  };

  beforeEach(() => {
    localStorage.clear();

    httpSpy = jasmine.createSpyObj('HttpClient', ['post']);
    jwtSpy = jasmine.createSpyObj('JwtHelperService', [
      'isTokenExpired',
      'decodeToken',
    ]);

    TestBed.configureTestingModule({
      providers: [
        { provide: HttpClient, useValue: httpSpy },
        { provide: JwtHelperService, useValue: jwtSpy },
      ],
    });

    service = TestBed.inject(AuthService);
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('deve limpar o estado se não houver token ou se token estiver expirado', fakeAsync(() => {
    jwtSpy.isTokenExpired.and.returnValue(Promise.resolve(true));

    service = new AuthService(httpSpy, jwtSpy);
    tick();

    expect(service.getToken()).toBeNull();

    let loggedIn = true;
    service.isLoggedIn$.subscribe((v) => (loggedIn = v));
    expect(loggedIn!).toBeFalse();

    let isAdmin = true;
    service.isAdmin$.subscribe((v) => (isAdmin = v));
    expect(isAdmin!).toBeFalse();
  }));

  it('deve carregar usuário do token válido no construtor', fakeAsync(() => {
    localStorage.setItem('accessToken', 'valid.token.here');
    jwtSpy.isTokenExpired.and.returnValue(Promise.resolve(false));
    jwtSpy.decodeToken.and.returnValue({ role: 'ADMIN' });

    service = new AuthService(httpSpy, jwtSpy);
    tick();

    let loggedInValue = false;
    service.isLoggedIn$.subscribe((v) => (loggedInValue = v));
    expect(loggedInValue!).toBeTrue();

    let isAdminValue = false;
    service.isAdmin$.subscribe((v) => (isAdminValue = v));
    expect(isAdminValue!).toBeTrue();
  }));

  describe('login()', () => {
    beforeEach(() => {
      localStorage.clear();
    });

    it('deve armazenar tokens e atualizar subjects em caso de sucesso', fakeAsync(() => {
      httpSpy.post.and.returnValue(of(dummyAuthResponse));
      jwtSpy.decodeToken.and.returnValue(dummyDecodedToken);

      service.login('user', 'pass').subscribe({
        next: (res) => {
          expect(res).toEqual(dummyAuthResponse);
        },
        error: () => {
          fail('Não esperava erro');
        },
      });
      tick();

      expect(localStorage.getItem('accessToken')).toBe(
        dummyAuthResponse.accessToken
      );
      expect(localStorage.getItem('refreshToken')).toBe(
        dummyAuthResponse.refreshToken
      );

      let loggedIn = false;
      service.isLoggedIn$.subscribe((v) => (loggedIn = v));
      expect(loggedIn!).toBeTrue();

      let isAdmin = false;
      service.isAdmin$.subscribe((v) => (isAdmin = v));
      expect(isAdmin!).toBeTrue();
    }));

    it('deve emitir erro "Erro no login" em caso de falha HTTP', fakeAsync(() => {
      httpSpy.post.and.returnValue(
        throwError(() => new HttpErrorResponse({ status: 401 }))
      );

      service.login('user', 'wrong').subscribe({
        next: () => {
          fail('Não esperava sucesso');
        },
        error: (err) => {
          expect(err.message).toBe('Erro no login');
        },
      });
      tick();
    }));
  });

  describe('register()', () => {
    it('deve completar sem emitir valor em caso de sucesso', fakeAsync(() => {
      httpSpy.post.and.returnValue(of({}));
      service.register('joao', 'senha', 'USER').subscribe({
        next: (res) => {
          expect(res).toBeUndefined();
        },
        complete: () => {},
        error: () => {
          fail('Não esperava erro');
        },
      });
      tick();
    }));

    it('deve emitir erro "Usuário já existe" quando status 409', fakeAsync(() => {
      const httpError = new HttpErrorResponse({ status: 409 });
      httpSpy.post.and.returnValue(throwError(() => httpError));
      service.register('joao', 'senha', 'USER').subscribe({
        next: () => {
          fail('Não esperava sucesso');
        },
        error: (err) => {
          expect(err.message).toBe('Usuário já existe');
        },
      });
      tick();
    }));

    it('deve emitir erro genérico em outro status', fakeAsync(() => {
      const httpError = new HttpErrorResponse({ status: 500 });
      httpSpy.post.and.returnValue(throwError(() => httpError));
      service.register('joao', 'senha', 'USER').subscribe({
        next: () => {
          fail('Não esperava sucesso');
        },
        error: (err) => {
          expect(err.message).toBe('Erro ao registrar usuário');
        },
      });
      tick();
    }));
  });

  describe('logout()', () => {
    it('deve limpar localStorage e atualizar subjects', () => {
      localStorage.setItem('accessToken', 'token');
      localStorage.setItem('refreshToken', 'refresh');
      (service as any).loggedInSubject.next(true);
      (service as any).isAdminSubject.next(true);

      service.logout();

      expect(localStorage.getItem('accessToken')).toBeNull();
      expect(localStorage.getItem('refreshToken')).toBeNull();

      let loggedIn = true;
      service.isLoggedIn$.subscribe((v) => (loggedIn = v));
      expect(loggedIn!).toBeFalse();

      let isAdmin = true;
      service.isAdmin$.subscribe((v) => (isAdmin = v));
      expect(isAdmin!).toBeFalse();
    });
  });

  describe('getToken() / getRefreshToken()', () => {
    it('deve retornar null quando não há tokens', () => {
      localStorage.clear();
      expect(service.getToken()).toBeNull();
      expect(service.getRefreshToken()).toBeNull();
    });

    it('deve retornar valores corretos quando existem no localStorage', () => {
      localStorage.setItem('accessToken', 'abc');
      localStorage.setItem('refreshToken', 'xyz');
      expect(service.getToken()).toBe('abc');
      expect(service.getRefreshToken()).toBe('xyz');
    });
  });

  describe('refreshToken()', () => {
    beforeEach(() => {
      localStorage.clear();
    });

    it('deve rejeitar se não houver refreshToken', fakeAsync(() => {
      let caughtError: any;
      service
        .refreshToken()
        .then(() => {
          fail('Não esperava sucesso');
        })
        .catch((err) => {
          caughtError = err;
        });
      tick();
      expect(caughtError).toBe('Sem refresh token');
    }));

    it('deve renovar tokens em caso de sucesso HTTP', fakeAsync(() => {
      localStorage.setItem('refreshToken', 'oldRefresh');
      const newAuth: any = {
        accessToken: 'newAccess',
        refreshToken: 'newRefresh',
      };
      httpSpy.post.and.returnValue(of(newAuth));
      jwtSpy.decodeToken.and.returnValue({ role: 'ADMIN' });

      let resultToken: string | undefined;
      service
        .refreshToken()
        .then((token) => {
          resultToken = token;
        })
        .catch(() => {
          fail('Não esperava erro');
        });
      tick();

      expect(localStorage.getItem('accessToken')).toBe('newAccess');
      expect(localStorage.getItem('refreshToken')).toBe('newRefresh');

      let loggedIn = false;
      service.isLoggedIn$.subscribe((v) => (loggedIn = v));
      expect(loggedIn!).toBeTrue();

      let isAdmin = false;
      service.isAdmin$.subscribe((v) => (isAdmin = v));
      expect(isAdmin!).toBeTrue();

      expect(resultToken).toBe('newAccess');
    }));

    it('deve rejeitar e limpar dados se HTTP falhar', fakeAsync(() => {
      localStorage.setItem('refreshToken', 'oldRefresh');
      httpSpy.post.and.returnValue(
        throwError(() => new HttpErrorResponse({ status: 401 }))
      );
      spyOn(service as any, 'clearAuthData').and.callThrough();

      let caughtError: any;
      service
        .refreshToken()
        .then(() => {
          fail('Não esperava sucesso');
        })
        .catch((err) => {
          caughtError = err;
        });
      tick();
      expect((service as any).clearAuthData).toHaveBeenCalled();
      expect(caughtError).toBe('Não foi possível renovar token');

      let loggedIn = true;
      service.isLoggedIn$.subscribe((v) => (loggedIn = v));
      expect(loggedIn!).toBeFalse();

      let isAdmin = true;
      service.isAdmin$.subscribe((v) => (isAdmin = v));
      expect(isAdmin!).toBeFalse();
    }));
  });
});
