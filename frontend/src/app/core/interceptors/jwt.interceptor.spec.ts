import { TestBed } from '@angular/core/testing';
import { HttpClient, HTTP_INTERCEPTORS } from '@angular/common/http';
import { HttpTestingController } from '@angular/common/http/testing';
import { JwtInterceptor } from './jwt.interceptor';
import { AuthService } from '../../features/auth/auth.service';

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
    httpTestingController.verify();
    mockAuthService.setToken(null);
    localStorage.clear();
  });

  it('should create the interceptor', () => {
    const interceptor = TestBed.inject(JwtInterceptor);
    expect(interceptor).toBeTruthy();
  });

  it('should add Authorization header when token is present', () => {
    const fakeToken = 'fake-jwt-token';
    mockAuthService.setToken(fakeToken);

    httpClient.get('/test-url').subscribe((response) => {
      expect(response).toBeTruthy();
    });

    const req = httpTestingController.expectOne((request) => {
      return request.url === '/test-url';
    });

    expect(req.request.headers.has('Authorization')).toBeTrue();
    expect(req.request.headers.get('Authorization')).toBe(
      `Bearer ${fakeToken}`
    );

    req.flush({ data: 'ok' });
  });

  it('should NOT add Authorization header when token is absent', () => {
    mockAuthService.setToken(null);
    localStorage.removeItem('accessToken');

    httpClient.get('/test-url-no-token').subscribe((response) => {
      expect(response).toBeTruthy();
    });

    const req = httpTestingController.expectOne((request) => {
      return request.url === '/test-url-no-token';
    });

    expect(req.request.headers.has('Authorization')).toBeFalse();

    req.flush({ data: 'ok' });
  });
});
