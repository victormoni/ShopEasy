import { Component, OnInit, OnDestroy } from '@angular/core';
import { OrderService } from '../order.service';
import { OrderResponse } from '../order.model';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';
import { AuthService } from '../../auth/auth.service';

@Component({
  selector: 'app-order-list',
  templateUrl: './order-list.component.html',
  styleUrls: ['./order-list.component.scss'],
  standalone: true,
  imports: [CommonModule, RouterLink],
})
export class OrderListComponent implements OnInit, OnDestroy {
  orders: OrderResponse[] = [];
  totalElements = 0;
  page = 0;
  size = 10;
  loading = false;
  error: string | null = null;

  // ← NOVO: variável para habilitar/desabilitar o botão “Criar Pedido”
  isLoggedIn = false;

  private subscriptions: Subscription[] = [];

  constructor(
    private orderService: OrderService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    // 1) Inscreve no isLoggedIn$ do AuthService para saber se quem está logado
    const subLogin = this.authService.isLoggedIn$.subscribe((logged) => {
      this.isLoggedIn = logged;
    });
    this.subscriptions.push(subLogin);

    // 2) Carrega a lista de pedidos
    this.loadOrders();
  }

  loadOrders(): void {
    this.loading = true;
    this.error = null;

    const sub = this.orderService.listMyOrders(this.page, this.size).subscribe({
      next: (pageData) => {
        this.orders = pageData.content;
        this.totalElements = pageData.totalElements;
        this.loading = false;
      },
      error: (err) => {
        console.error('Erro ao carregar pedidos:', err);
        this.error = 'Não foi possível carregar seus pedidos.';
        this.loading = false;
      },
    });
    this.subscriptions.push(sub);
  }

  changePage(delta: number) {
    const newPage = this.page + delta;
    if (newPage < 0) {
      return;
    }
    const lastPage = Math.floor((this.totalElements - 1) / this.size);
    if (newPage > lastPage) {
      return;
    }
    this.page = newPage;
    this.loadOrders();
  }

  get totalPages(): number {
    return Math.ceil(this.totalElements / this.size);
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }
}
