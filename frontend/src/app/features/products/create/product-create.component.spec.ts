import {
  ComponentFixture,
  TestBed,
  fakeAsync,
  tick,
  waitForAsync,
} from '@angular/core/testing';
import { ProductCreateComponent } from './product-create.component';
import { ActivatedRoute, Router } from '@angular/router';
import { ProductService } from '../product.service';
import { AuthService } from '../../auth/auth.service';
import { of } from 'rxjs';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';

describe('ProductCreateComponent', () => {
  let component: ProductCreateComponent;
  let fixture: ComponentFixture<ProductCreateComponent>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let productServiceSpy: jasmine.SpyObj<ProductService>;
  let routerSpy: jasmine.SpyObj<Router>;

  beforeEach(waitForAsync(() => {
    const aSpy = jasmine.createSpyObj('AuthService', [], {
      isAdmin$: of(false),
    });
    const pSpy = jasmine.createSpyObj('ProductService', ['create']);
    const rSpy = jasmine.createSpyObj('Router', ['navigate']);
    const routeStub = {
      snapshot: {
        paramMap: { get: (key: string) => null },
      },
    };

    TestBed.configureTestingModule({
      imports: [
        ProductCreateComponent,
        CommonModule,
        ReactiveFormsModule,
        FormsModule,
      ],
      providers: [
        { provide: AuthService, useValue: aSpy },
        { provide: ProductService, useValue: pSpy },
        { provide: Router, useValue: rSpy },
        { provide: ActivatedRoute, useValue: routeStub },
      ],
    }).compileComponents();

    authServiceSpy = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    productServiceSpy = TestBed.inject(
      ProductService
    ) as jasmine.SpyObj<ProductService>;
    routerSpy = TestBed.inject(Router) as jasmine.SpyObj<Router>;
  }));

  it('se isAdmin$ emitir false, deve redirecionar para /products', fakeAsync(() => {
    fixture = TestBed.createComponent(ProductCreateComponent);
    component = fixture.componentInstance;

    fixture.detectChanges();
    tick();

    expect(routerSpy.navigate).toHaveBeenCalledWith(['/products']);
  }));
});
