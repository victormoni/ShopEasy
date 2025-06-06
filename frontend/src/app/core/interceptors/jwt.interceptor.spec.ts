import { TestBed } from '@angular/core/testing';
import { HttpClient, HTTP_INTERCEPTORS } from '@angular/common/http';
import {
  HttpTestingController,
} from '@angular/common/http/testing';

import { JwtInterceptor } from './jwt.interceptor';
import { AuthService } from '../../features/auth/auth.service';

// Mock simples para AuthService que retorna sempre um token ou null
class MockAuthService {
  private token: string | null = null;

  setToken(tok: string | null) {
    this.token = tok;
  }

  getToken(): string | null {
    return this.token;
  }
}

describe('JwtInterceptor', () => {
  let httpClient: HttpClient;
  let httpTestingController: HttpTestingController;
  let mockAuthService: MockAuthService;

  beforeEach(() => {
    mockAuthService = new MockAuthService();

    TestBed.configureTestingModule({
      providers: [
        // Substitui o AuthService real pelo nosso mock
        { provide: AuthService, useValue: mockAuthService },
        {
          provide: HTTP_INTERCEPTORS,
          useClass: JwtInterceptor,
          multi: true,
        },
      ],
    });

    httpClient = TestBed.inject(HttpClient);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    // Verifica que não há requisições pendentes
    httpTestingController.verify();
    // Limpa o token armazenado
    mockAuthService.setToken(null);
    localStorage.clear();
  });

  it('should create the interceptor', () => {
    const interceptor = TestBed.inject(JwtInterceptor);
    expect(interceptor).toBeTruthy();
  });

  it('should add Authorization header when token is present', () => {
    // 1) Configura nosso mockAuthService para retornar um token qualquer
    const fakeToken = 'fake-jwt-token';
    mockAuthService.setToken(fakeToken);
    // Também pode simular que o token esteja no localStorage, se seu AuthService ler de lá:
    // localStorage.setItem('accessToken', fakeToken);

    // 2) Disparamos uma requisição GET qualquer
    httpClient.get('/test-url').subscribe((response) => {
      // O corpo da resposta não importa para este teste
      expect(response).toBeTruthy();
    });

    // 3) Espera o HttpTestingController capturar a requisição
    const req = httpTestingController.expectOne((request) => {
      return request.url === '/test-url';
    });

    // 4) Verifica se o header Authorization foi adicionado corretamente
    expect(req.request.headers.has('Authorization')).toBeTrue();
    expect(req.request.headers.get('Authorization')).toBe(
      `Bearer ${fakeToken}`
    );

    // 5) Retorna um dummy response para completar a chamada
    req.flush({ data: 'ok' });
  });

  it('should NOT add Authorization header when token is absent', () => {
    // Garante que o mockAuthService devolve null (nenhum token)
    mockAuthService.setToken(null);
    localStorage.removeItem('accessToken');

    // Disparamos a mesma requisição GET
    httpClient.get('/test-url-no-token').subscribe((response) => {
      expect(response).toBeTruthy();
    });

    // Capta a requisição
    const req = httpTestingController.expectOne((request) => {
      return request.url === '/test-url-no-token';
    });

    // Verifica que o header Authorization NÃO existe
    expect(req.request.headers.has('Authorization')).toBeFalse();

    // Retorna um dummy response
    req.flush({ data: 'ok' });
  });
});
