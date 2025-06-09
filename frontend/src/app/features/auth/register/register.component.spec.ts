import { RouterTestingModule } from '@angular/router/testing';
import {
  ComponentFixture,
  TestBed,
  fakeAsync,
  tick,
} from '@angular/core/testing';
import { RegisterComponent } from './register.component';
import { AuthService } from '../auth.service';
import { Router } from '@angular/router';
import { ReactiveFormsModule } from '@angular/forms';
import { of, throwError } from 'rxjs';
import { ActivatedRoute } from '@angular/router';
import { By } from '@angular/platform-browser';

describe('RegisterComponent', () => {
  let component: RegisterComponent;
  let fixture: ComponentFixture<RegisterComponent>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let routerSpy: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    const authSpy = jasmine.createSpyObj('AuthService', ['register']);
    const rtrSpy = jasmine.createSpyObj('Router', ['navigate']);
    const routeStub = { snapshot: { paramMap: { get: () => null } } };

    await TestBed.configureTestingModule({
      imports: [RegisterComponent, ReactiveFormsModule, RouterTestingModule],
      providers: [
        { provide: AuthService, useValue: authSpy },
        { provide: Router, useValue: rtrSpy },
        { provide: ActivatedRoute, useValue: routeStub },
      ],
    }).compileComponents();

    authServiceSpy = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    routerSpy = TestBed.inject(Router) as jasmine.SpyObj<Router>;

    fixture = TestBed.createComponent(RegisterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('deve criar o componente', () => {
    expect(component).toBeTruthy();
  });

  it('form deve ser inválido inicialmente', () => {
    expect(component.registerForm.valid).toBeFalse();
    const controls = component.registerForm.controls;
    expect(controls['username'].value).toBe('');
    expect(controls['password'].value).toBe('');
    expect(controls['role'].value).toBe('USER');
  });

  it('onSubmit não deve chamar AuthService.register se o form for inválido', () => {
    component.onSubmit();
    expect(authServiceSpy.register).not.toHaveBeenCalled();
  });

  it('onSubmit deve chamar AuthService.register e exibir mensagem de sucesso', fakeAsync(() => {
    component.registerForm.setValue({
      username: 'joao',
      password: 'senha123',
      role: 'USER',
    });
    authServiceSpy.register.and.returnValue(of());

    component.onSubmit();
    tick();

    expect(authServiceSpy.register).toHaveBeenCalledWith(
      'joao',
      'senha123',
      'USER'
    );
    expect(component.success).toBe(
      'Usuário registrado com sucesso! Você pode fazer login agora.'
    );

    tick(2000);
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/login']);
  }));

  it('onSubmit deve exibir mensagem de "usuário já existe" quando status for 409', fakeAsync(() => {
    component.registerForm.setValue({
      username: 'joao',
      password: 'senha123',
      role: 'USER',
    });
    authServiceSpy.register.and.returnValue(throwError({ status: 409 }));

    component.onSubmit();
    tick();

    expect(component.error).toBe('Usuário já existe. Escolha outro nome.');
    expect(component.success).toBeNull();
    expect(routerSpy.navigate).not.toHaveBeenCalled();
  }));

  it('onSubmit deve exibir mensagem genérica em caso de outro erro', fakeAsync(() => {
    component.registerForm.setValue({
      username: 'joao',
      password: 'senha123',
      role: 'USER',
    });
    authServiceSpy.register.and.returnValue(throwError({ status: 500 }));

    component.onSubmit();
    tick();

    expect(component.error).toBe('Erro ao registrar usuário.');
    expect(component.success).toBeNull();
    expect(routerSpy.navigate).not.toHaveBeenCalled();
  }));

  it('template deve exibir mensagens de erro e sucesso corretamente', fakeAsync(() => {
    component.error = 'Erro qualquer';
    fixture.detectChanges();
    let errorElem = fixture.debugElement.query(By.css('p.error'));
    expect(errorElem).toBeTruthy();
    expect(errorElem.nativeElement.textContent).toContain('Erro qualquer');

    component.error = null;
    component.success = 'Tudo OK';
    fixture.detectChanges();
    const successElem = fixture.debugElement.query(By.css('p.success'));
    expect(successElem).toBeTruthy();
    expect(successElem.nativeElement.textContent).toContain('Tudo OK');
  }));
});
