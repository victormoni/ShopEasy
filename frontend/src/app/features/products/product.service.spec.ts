import { TestBed } from '@angular/core/testing';
import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';
import { ProductService } from './product.service';
import { PageProductResponse, ProductResponse } from './product.model';
import { environment } from '../../../environments/environment';

describe('ProductService', () => {
  let service: ProductService;
  let httpMock: HttpTestingController;

  const dummyPageResponse: PageProductResponse = {
    content: [
      {
        id: 1,
        name: 'Produto A',
        description: 'Descrição A',
        price: 50.0,
        stock: 10,
        category: 'CAT1',
        createdAt: '02/06/2025 12:00',
      },
      {
        id: 2,
        name: 'Produto B',
        description: 'Descrição B',
        price: 100.0,
        stock: 5,
        category: 'CAT2',
        createdAt: '02/06/2025 13:00',
      },
    ],
    totalElements: 2,
    totalPages: 1,
    number: 0,
    size: 10,
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ProductService],
    });

    service = TestBed.inject(ProductService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('deve ser criado', () => {
    expect(service).toBeTruthy();
  });

  it('deve chamar GET /api/products sem filtros (página padrão)', () => {
    service
      .list(undefined, undefined, undefined, undefined, 0, 10)
      .subscribe((res) => {
        expect(res).toEqual(dummyPageResponse);
      });

    const req = httpMock.expectOne(
      (req) =>
        req.method === 'GET' &&
        req.url === `${environment.apiUrl}/api/products` &&
        req.params.get('page') === '0' &&
        req.params.get('size') === '10'
    );

    req.flush(dummyPageResponse);
  });

  it('deve passar parâmetros de filtro e paginação corretamente', () => {
    service.list('abc', 'CAT1', 10, 100, 2, 5).subscribe((res) => {
      expect(res.content.length).toBe(2);
      expect(res.totalElements).toBe(2);
    });

    const req = httpMock.expectOne((request) => {
      const params = request.params;
      return (
        request.method === 'GET' &&
        request.url === `${environment.apiUrl}/api/products` &&
        params.get('name') === 'abc' &&
        params.get('category') === 'CAT1' &&
        params.get('minPrice') === '10' &&
        params.get('maxPrice') === '100' &&
        params.get('page') === '2' &&
        params.get('size') === '5'
      );
    });

    req.flush(dummyPageResponse);
  });
});
