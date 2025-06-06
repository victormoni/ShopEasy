import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PageProductResponse, Product, ProductRequest } from './product.model';

@Injectable({
  providedIn: 'root',
})
export class ProductService {
  private baseUrl = `${environment.apiUrl}/api/products`;

  constructor(private http: HttpClient) {}

  list(
    name?: string,
    category?: string,
    minPrice?: number,
    maxPrice?: number,
    page: number = 0,
    size: number = 10
  ): Observable<PageProductResponse> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (name && name.trim().length > 0) {
      params = params.set('name', name.trim());
    }
    if (category && category.trim().length > 0) {
      params = params.set('category', category.trim());
    }
    if (minPrice != null && !isNaN(minPrice)) {
      params = params.set('minPrice', minPrice.toString());
    }
    if (maxPrice != null && !isNaN(maxPrice)) {
      params = params.set('maxPrice', maxPrice.toString());
    }

    return this.http.get<PageProductResponse>(this.baseUrl, { params });
  }

  getById(id: number): Observable<Product> {
    return this.http.get<Product>(`${this.baseUrl}/${id}`);
  }

  create(dto: ProductRequest): Observable<Product> {
    return this.http.post<Product>(this.baseUrl, dto);
  }

  update(id: number, dto: ProductRequest): Observable<Product> {
    return this.http.put<Product>(`${this.baseUrl}/${id}`, dto);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
