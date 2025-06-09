import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HeaderComponent } from '../../../core/components/header/header.component';
import { AuthService } from '../../../features/auth/auth.service';
import { of } from 'rxjs';
import { By } from '@angular/platform-browser';

describe('HeaderComponent', () => {
  let component: HeaderComponent;
  let fixture: ComponentFixture<HeaderComponent>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;

  beforeEach(async () => {
    const spy = jasmine.createSpyObj('AuthService', [], {
      isLoggedIn$: of(true),
    });

    await TestBed.configureTestingModule({
      providers: [{ provide: AuthService, useValue: spy }],
    }).compileComponents();

    authServiceSpy = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    fixture = TestBed.createComponent(HeaderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('deve exibir botão de logout quando isLoggedIn$ emitir true', () => {
    fixture.detectChanges();
    const btnLogout = fixture.debugElement.query(By.css('button.logout'));
    expect(btnLogout).toBeTruthy();
  });

  it('logout deve chamar authService.logout()', () => {
    component.logout();
    expect(authServiceSpy.logout).toHaveBeenCalled();
  });
});
