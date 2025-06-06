import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { of } from 'rxjs';
import { AppComponent } from './app.component';
import { AuthService } from './features/auth/auth.service';

@Component({
  selector: 'app-header',
  template: '',
  standalone: true, // necessário para uso em imports
})
class HeaderStubComponent {
  @Input() isAdmin!: boolean;
}

describe('AppComponent', () => {
  let fixture: ComponentFixture<AppComponent>;
  let component: AppComponent;
  let authServiceSpy: jasmine.SpyObj<AuthService>;

  beforeEach(waitForAsync(() => {
    const spy = jasmine.createSpyObj('AuthService', [], {
      isLoggedIn$: of(false),
      isAdmin$: of(false),
    });

    TestBed.configureTestingModule({
      imports: [CommonModule, AppComponent, HeaderStubComponent],
      providers: [{ provide: AuthService, useValue: spy }],
    }).compileComponents();

    authServiceSpy = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    fixture = TestBed.createComponent(AppComponent);
    component = fixture.componentInstance;
  }));

  it('deve criar o componente', () => {
    expect(component).toBeTruthy();
  });

  it('ngOnInit: quando isLoggedIn$ emitir true, deve chamar isAdmin$ e emitir seu valor', (done) => {
    authServiceSpy.isLoggedIn$ = of(true);
    authServiceSpy.isAdmin$ = of(true);

    component.ngOnInit();
    component.isAdmin$.subscribe((value) => {
      expect(authServiceSpy.isLoggedIn$).toBeDefined();
      expect(authServiceSpy.isAdmin$).toBeDefined();
      expect(value).toBeTrue();
      done();
    });
  });

  it('ngOnInit: quando isLoggedIn$ emitir false, isAdmin$ não deve ser consultado nem emitir valor', (done) => {
    authServiceSpy.isLoggedIn$ = of(false);
    authServiceSpy.isAdmin$ = of(true);

    component.ngOnInit();
    let emitted = false;

    const sub = component.isAdmin$.subscribe({
      next: () => {
        emitted = true;
      },
      complete: () => {
        expect(emitted).toBeFalse();
        done();
      },
    });

    setTimeout(() => {
      sub.unsubscribe();
      expect(emitted).toBeFalse();
      done();
    }, 0);
  });
});
