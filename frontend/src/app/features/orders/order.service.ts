import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { OrderRequest, OrderResponse } from './order.model';

@Injectable({
  providedIn: 'root',
})
export class OrderService {
  private baseUrl = `${environment.apiUrl}/api/orders`;

  constructor(private http: HttpClient) {}

  create(order: OrderRequest): Observable<OrderResponse> {
    return this.http.post<OrderResponse>(this.baseUrl, order);
  }

  listMyOrders(
    page = 0,
    size = 10
  ): Observable<{ content: OrderResponse[]; totalElements: number }> {
    const url = `${this.baseUrl}/me?page=${page}&size=${size}`;
    return this.http.get<{ content: OrderResponse[]; totalElements: number }>(
      url
    );
  }

  getOrderById(id: number): Observable<OrderResponse> {
    const url = `${this.baseUrl}/${id}`;
    return this.http.get<OrderResponse>(url);
  }
}
