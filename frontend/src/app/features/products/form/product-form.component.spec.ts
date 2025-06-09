import {
  ComponentFixture,
  TestBed,
  fakeAsync,
  tick,
  waitForAsync,
} from '@angular/core/testing';
import { ProductFormComponent } from './product-form.component';
import { ProductService } from '../product.service';
import { ProductRequest } from '../product.model';
import { Router } from '@angular/router';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { of, throwError } from 'rxjs';
import { CommonModule } from '@angular/common';
import { By } from '@angular/platform-browser';

describe('ProductFormComponent', () => {
  let component: ProductFormComponent;
  let fixture: ComponentFixture<ProductFormComponent>;
  let productServiceSpy: jasmine.SpyObj<ProductService>;
  let routerSpy: jasmine.SpyObj<Router>;

  beforeEach(waitForAsync(() => {
    const prodSpy = jasmine.createSpyObj('ProductService', ['create']);
    const rtrSpy = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      imports: [CommonModule, ReactiveFormsModule, FormsModule],
      providers: [
        { provide: ProductService, useValue: prodSpy },
        { provide: Router, useValue: rtrSpy },
      ],
    }).compileComponents();

    productServiceSpy = TestBed.inject(
      ProductService
    ) as jasmine.SpyObj<ProductService>;
    routerSpy = TestBed.inject(Router) as jasmine.SpyObj<Router>;

    fixture = TestBed.createComponent(ProductFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('deve criar o componente', () => {
    expect(component).toBeTruthy();
  });

  it('form deve ser inválido inicialmente', () => {
    const form = component.productForm;
    expect(form.valid).toBeFalse();
    expect(form.get('name')?.value).toBe('');
    expect(form.get('description')?.value).toBe('');
    expect(form.get('price')?.value).toBeNull();
    expect(form.get('stock')?.value).toBeNull();
  });

  it('campo "name" deve ser obrigatório e ter no máximo 100 caracteres', () => {
    const nameControl = component.productForm.get('name')!;
    nameControl.setValue('');
    expect(nameControl.hasError('required')).toBeTrue();

    const longString = 'a'.repeat(101);
    nameControl.setValue(longString);
    expect(nameControl.hasError('maxlength')).toBeTrue();

    nameControl.setValue('Nome válido');
    expect(nameControl.valid).toBeTrue();
  });

  it('campo "description" deve ter no máximo 1000 caracteres', () => {
    const descControl = component.productForm.get('description')!;
    const longDesc = 'a'.repeat(1001);
    descControl.setValue(longDesc);
    expect(descControl.hasError('maxlength')).toBeTrue();

    descControl.setValue('Descrição aceitável');
    expect(descControl.valid).toBeTrue();
  });

  it('campo "price" deve ser obrigatório e maior ou igual a zero', () => {
    const priceControl = component.productForm.get('price')!;
    priceControl.setValue(null);
    expect(priceControl.hasError('required')).toBeTrue();

    priceControl.setValue(-10);
    expect(priceControl.hasError('min')).toBeTrue();

    priceControl.setValue(0);
    expect(priceControl.valid).toBeTrue();

    priceControl.setValue(25.5);
    expect(priceControl.valid).toBeTrue();
  });

  it('campo "stock" deve ser obrigatório e maior ou igual a zero', () => {
    const stockControl = component.productForm.get('stock')!;
    stockControl.setValue(null);
    expect(stockControl.hasError('required')).toBeTrue();

    stockControl.setValue(-5);
    expect(stockControl.hasError('min')).toBeTrue();

    stockControl.setValue(0);
    expect(stockControl.valid).toBeTrue();

    stockControl.setValue(10);
    expect(stockControl.valid).toBeTrue();
  });

  it('onSubmit não deve chamar service.create se form for inválido', () => {
    component.onSubmit();
    expect(productServiceSpy.create).not.toHaveBeenCalled();
    expect(routerSpy.navigate).not.toHaveBeenCalled();
  });

  it('onSubmit deve chamar service.create e navegar em caso de sucesso', fakeAsync(() => {
    component.productForm.setValue({
      name: 'Produto X',
      description: 'Descrição X',
      price: 75,
      stock: 20,
    });
    expect(component.productForm.valid).toBeTrue();

    productServiceSpy.create.and.returnValue(of({} as any));

    component.onSubmit();
    tick();
    expect(productServiceSpy.create).toHaveBeenCalledWith({
      name: 'Produto X',
      description: 'Descrição X',
      price: 75,
      stock: 20,
    } as ProductRequest);
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/products']);
  }));

  it('onSubmit deve setar error quando service.create falhar', fakeAsync(() => {
    component.productForm.setValue({
      name: 'Produto Y',
      description: 'Descrição Y',
      price: 50,
      stock: 5,
    });
    expect(component.productForm.valid).toBeTrue();

    productServiceSpy.create.and.returnValue(throwError({ status: 500 }));

    component.onSubmit();
    tick();
    expect(productServiceSpy.create).toHaveBeenCalled();
    expect(component.error).toBe('Erro ao criar produto. Verifique os dados.');
    expect(routerSpy.navigate).not.toHaveBeenCalled();
  }));

  it('template não deve exibir mensagem de erro inicialmente', () => {
    fixture.detectChanges();
    const errorElem = fixture.debugElement.query(By.css('p.error'));
    expect(errorElem).toBeNull();
  });

  it('template deve exibir mensagem de erro quando component.error for definido', () => {
    component.error = 'Falha no servidor';
    fixture.detectChanges();
    const errorElem = fixture.debugElement.query(By.css('p.error'));
    expect(errorElem).toBeTruthy();
    expect(errorElem.nativeElement.textContent).toContain('Falha no servidor');
  });
});
