import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { ProductListComponent } from './product-list.component';
import { ProductService } from '../product.service';
import { PageProductResponse } from '../product.model';
import { of } from 'rxjs';
import { By } from '@angular/platform-browser';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

describe('ProductListComponent', () => {
  let component: ProductListComponent;
  let fixture: ComponentFixture<ProductListComponent>;
  let productServiceSpy: jasmine.SpyObj<ProductService>;

  const dummyPage: PageProductResponse = {
    content: [
      {
        id: 1,
        name: 'Produto X',
        description: 'Desc X',
        price: 10.0,
        stock: 5,
        category: 'CATX',
        createdAt: '02/06/2025 10:00',
      },
      {
        id: 2,
        name: 'Produto Y',
        description: 'Desc Y',
        price: 20.0,
        stock: 3,
        category: 'CATY',
        createdAt: '02/06/2025 11:00',
      },
    ],
    totalElements: 2,
    totalPages: 1,
    number: 0,
    size: 10,
  };

  beforeEach(waitForAsync(() => {
    // Cria um SpyObj de ProductService com método list spy
    const spy = jasmine.createSpyObj('ProductService', ['list']);

    TestBed.configureTestingModule({
      imports: [
        CommonModule,
        FormsModule, 
        HttpClientTestingModule // para [(ngModel)] nos filtros
      ],
      providers: [{ provide: ProductService, useValue: spy }],
    }).compileComponents();

    productServiceSpy = TestBed.inject(
      ProductService
    ) as jasmine.SpyObj<ProductService>;

    // Faz o spy retornar um observable de dummyPage
    productServiceSpy.list.and.returnValue(of(dummyPage));

    // Cria componente e detecta mudanças
    fixture = TestBed.createComponent(ProductListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges(); // dispara ngOnInit e a chamada ao serviço
  }));

  it('deve criar o componente', () => {
    expect(component).toBeTruthy();
  });

  it('deve chamar ProductService.list() no ngOnInit', () => {
    // O spy foi configurado para retornar of(dummyPage). Verifica se foi chamado com parâmetros iniciais:
    expect(productServiceSpy.list).toHaveBeenCalledWith(
      '', // filterName
      '', // filterCategory
      undefined, // filterMinPrice
      undefined, // filterMaxPrice
      0, // page
      10 // size
    );
  });

  it('deve preencher products com pageData.content', () => {
    expect(component.products.length).toBe(2);
    expect(component.products[0].name).toBe('Produto X');
    expect(component.totalPages).toBe(1);
    expect(component.totalElements).toBe(2);
  });

  it('deve exibir uma linha para cada produto na tabela', () => {
    // Aguarda template renderizar após detectChanges
    fixture.detectChanges();
    // Seleciona todas as linhas <tr> do <tbody>
    const rows = fixture.debugElement.queryAll(By.css('table tbody tr'));
    expect(rows.length).toBe(2);

    // Verifica conteúdo da primeira célula da primeira linha
    const primeiraCelula = rows[0].query(By.css('td')).nativeElement
      .textContent;
    expect(primeiraCelula).toContain('Produto X');
  });

  it('ao chamar changePage, deve requisitar nova lista com page atualizada', () => {
    // Ajusta o spy para retornar outra página simulada
    const newPage: PageProductResponse = {
      content: [
        {
          id: 3,
          name: 'Produto Z',
          description: 'Desc Z',
          price: 30.0,
          stock: 2,
          category: 'CATZ',
          createdAt: '02/06/2025 12:00',
        },
      ],
      totalElements: 3,
      totalPages: 2,
      number: 1,
      size: 10,
    };
    // No primeiro ngOnInit, já foi chamado dummyPage. Agora resetamos o retorno:
    productServiceSpy.list.calls.reset();
    productServiceSpy.list.and.returnValue(of(newPage));

    component.changePage(1); // passa de página 0 → 1
    expect(component.page).toBe(1);
    expect(productServiceSpy.list).toHaveBeenCalledWith(
      '',
      '',
      undefined,
      undefined,
      1,
      10
    );
    // Simular detecção de mudanças e atualização do template:
    fixture.detectChanges();
    expect(component.products[0].name).toBe('Produto Z');
  });
});
