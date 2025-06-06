import { TestBed, waitForAsync } from '@angular/core/testing';
import { Router, UrlTree } from '@angular/router';
import { of } from 'rxjs';
import { AuthGuard } from './auth.guard';
import { AuthService } from '../../features/auth/auth.service';

describe('AuthGuard', () => {
  let guard: AuthGuard;
  let authServiceMock: jasmine.SpyObj<AuthService>;
  let routerMock: jasmine.SpyObj<Router>;

  beforeEach(() => {
    // Criamos um SpyObj sem métodos (segundo parâmetro),
    // mas com a propriedade isLoggedIn$ (terceiro parâmetro é um objeto de getters).
    const spyAuth = jasmine.createSpyObj(
      'AuthService',
      [], // não há métodos para spiar
      {
        // inicialmente, isLoggedIn$ emite true
        isLoggedIn$: of(true),
      }
    );

    // Spy do Router, apenas para parseUrl()
    const spyRouter = jasmine.createSpyObj('Router', ['parseUrl']);

    TestBed.configureTestingModule({
      providers: [
        AuthGuard,
        { provide: AuthService, useValue: spyAuth },
        { provide: Router, useValue: spyRouter },
      ],
    });

    guard = TestBed.inject(AuthGuard);
    authServiceMock = TestBed.inject(
      AuthService
    ) as jasmine.SpyObj<AuthService>;
    routerMock = TestBed.inject(Router) as jasmine.SpyObj<Router>;
  });

  it('deve permitir ativação quando isLoggedIn$ emitir true', waitForAsync(() => {
    // Configura isLoggedIn$ para emitir true
    authServiceMock.isLoggedIn$ = of(true);

    guard.canActivate(null as any, null as any).subscribe((result) => {
      expect(result).toBeTrue();
    });
  }));

  it('deve redirecionar para /login quando isLoggedIn$ emitir false', waitForAsync(() => {
    // Configura isLoggedIn$ para emitir false
    authServiceMock.isLoggedIn$ = of(false);

    // Faz o parseUrl retornar um UrlTree fictício para '/login'
    const fakeTree = {} as UrlTree;
    routerMock.parseUrl.and.returnValue(fakeTree);

    guard.canActivate(null as any, null as any).subscribe((result) => {
      expect(routerMock.parseUrl).toHaveBeenCalledWith('/login');
      expect(result).toBe(fakeTree);
    });
  }));
});
