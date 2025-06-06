import {
  ComponentFixture,
  TestBed,
  waitForAsync,
  fakeAsync,
  tick,
} from '@angular/core/testing';
import { OrderListComponent } from './order-list.component';
import { OrderService } from '../order.service';
import { AuthService } from '../../auth/auth.service';
import { OrderResponse } from '../order.model';
import { of, throwError } from 'rxjs';
import { CommonModule } from '@angular/common';
import { By } from '@angular/platform-browser';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('OrderListComponent', () => {
  let component: OrderListComponent;
  let fixture: ComponentFixture<OrderListComponent>;
  let orderServiceSpy: jasmine.SpyObj<OrderService>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;

  const dummyOrders: OrderResponse[] = [
    {
      id: 1,
      createdAt: '03/06/2025 10:00',
      total: 150.0,
      items: [
        {
          id: 11,
          productId: 101,
          productName: 'Produto A',
          quantity: 2,
          unitPrice: 50.0,
          total: 100.0,
        },
        {
          id: 12,
          productId: 102,
          productName: 'Produto B',
          quantity: 1,
          unitPrice: 50.0,
          total: 50.0,
        },
      ],
    },
    {
      id: 2,
      createdAt: '03/06/2025 11:30',
      total: 80.0,
      items: [
        {
          id: 21,
          productId: 103,
          productName: 'Produto C',
          quantity: 2,
          unitPrice: 40.0,
          total: 80.0,
        },
      ],
    },
  ];

  const dummyPageResponse = {
    content: dummyOrders,
    totalElements: 2,
  };

  beforeEach(waitForAsync(() => {
    const orderSpy = jasmine.createSpyObj('OrderService', ['listMyOrders']);
    const authSpy = jasmine.createSpyObj('AuthService', [], {
      isLoggedIn$: of(true),
      isAdmin$: of(true),
    });

    TestBed.configureTestingModule({
      imports: [CommonModule, HttpClientTestingModule],
      providers: [
        { provide: OrderService, useValue: orderSpy },
        { provide: AuthService, useValue: authSpy },
      ],
    }).compileComponents();

    orderServiceSpy = TestBed.inject(
      OrderService
    ) as jasmine.SpyObj<OrderService>;
    authServiceSpy = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;

    orderServiceSpy.listMyOrders.and.returnValue(of(dummyPageResponse));

    fixture = TestBed.createComponent(OrderListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('deve criar o componente', () => {
    expect(component).toBeTruthy();
  });

  it('ngOnInit deve chamar listMyOrders com page=0 e size=10', () => {
    expect(orderServiceSpy.listMyOrders).toHaveBeenCalledWith(0, 10);
    expect(component.orders).toEqual(dummyOrders);
    expect(component.totalElements).toBe(2);
    expect(component.loading).toBeFalse();
    expect(component.error).toBeNull();
  });

  it('deve exibir duas linhas na tabela de pedidos', () => {
    fixture.detectChanges();
    const rows = fixture.debugElement.queryAll(By.css('table tbody tr'));
    expect(rows.length).toBe(2);

    const firstCells = rows[0].queryAll(By.css('td'));
    expect(firstCells[0].nativeElement.textContent).toContain('1');
    expect(firstCells[1].nativeElement.textContent).toContain(
      '03/06/2025 10:00'
    );
    expect(firstCells[2].nativeElement.textContent).toContain('150,00');
    expect(firstCells[3].nativeElement.textContent).toContain('2 itens');
  });

  it('deve mostrar mensagem de erro quando listMyOrders falhar', fakeAsync(() => {
    orderServiceSpy.listMyOrders.and.returnValue(throwError({ status: 500 }));
    component.loadOrders();
    tick();
    fixture.detectChanges();

    expect(component.error).toBe('Não foi possível carregar seus pedidos.');
    expect(component.loading).toBeFalse();

    const errorElem = fixture.debugElement.query(By.css('.error-message'));
    expect(errorElem).toBeTruthy();
    expect(errorElem.nativeElement.textContent).toContain(
      'Não foi possível carregar seus pedidos.'
    );
  }));

  it('changePage deve chamar loadOrders com nova página válida', fakeAsync(() => {
    const newPageResponse = { content: [], totalElements: 2 };
    orderServiceSpy.listMyOrders.calls.reset();
    orderServiceSpy.listMyOrders.and.returnValue(of(newPageResponse));

    component.page = 0;
    component.totalElements = 20;
    component.changePage(1);
    tick();
    fixture.detectChanges();

    expect(component.page).toBe(1);
    expect(orderServiceSpy.listMyOrders).toHaveBeenCalledWith(1, 10);
    expect(component.orders).toEqual([]);
  }));

  it('changePage não deve avançar se nova página for negativa', fakeAsync(() => {
    component.page = 0;
    component.changePage(-1);
    tick();
    fixture.detectChanges();

    expect(component.page).toBe(0);
    expect(orderServiceSpy.listMyOrders).toHaveBeenCalledTimes(1);
  }));

  it('changePage não deve avançar além da última página', fakeAsync(() => {
    component.page = 1;
    component.totalElements = 20;
    component.changePage(1);
    tick();
    fixture.detectChanges();

    expect(component.page).toBe(1);
    expect(orderServiceSpy.listMyOrders).toHaveBeenCalledTimes(1);
  }));

  it('totalPages deve retornar ceil(totalElements/size)', () => {
    component.totalElements = 25;
    component.size = 10;
    expect(component.totalPages).toBe(3);
  });
});
