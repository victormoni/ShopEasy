import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { OrderService } from '../order.service';
import { OrderResponse, OrderItemResponse } from '../order.model';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-order-detail',
  templateUrl: './order-detail.component.html',
  styleUrls: ['./order-detail.component.scss'],
  standalone: true,
  imports: [CommonModule],
})
export class OrderDetailComponent implements OnInit, OnDestroy {
  orderId!: number;
  order!: OrderResponse;
  loading = false;
  error: string | null = null;
  private subscription!: Subscription;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private orderService: OrderService
  ) {}

  ngOnInit(): void {
    this.orderId = Number(this.route.snapshot.paramMap.get('id'));
    if (isNaN(this.orderId)) {
      this.error = 'ID de pedido inválido.';
      return;
    }
    this.loadOrder();
  }

  loadOrder(): void {
    this.loading = true;
    this.error = null;
    this.subscription = this.orderService.getOrderById(this.orderId).subscribe({
      next: (data) => {
        this.order = data;
        this.loading = false;
      },
      error: (err) => {
        if (err.status === 404) {
          this.error = 'Pedido não encontrado.';
        } else if (err.status === 403) {
          this.error = 'Você não tem permissão para ver este pedido.';
        } else {
          this.error = 'Erro ao carregar pedido.';
        }
        this.loading = false;
      },
    });
  }

  getItemSubtotal(item: OrderItemResponse): number {
    return item.unitPrice * item.quantity;
  }

  goBack(): void {
    this.router.navigate(['/orders']);
  }

  ngOnDestroy(): void {
    this.subscription?.unsubscribe();
  }
}
