import { Component, OnInit } from '@angular/core';
import { ProductService } from '../../products/product.service';
import { Product } from '../../products/product.model';
import { OrderService } from '../order.service';
import { OrderRequest, OrderItemRequest, CartItem } from '../order.model';
import { Router } from '@angular/router';
import { PageProductResponse } from '../../products/product.model';
import { Observable, BehaviorSubject, switchMap, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-order-create',
  templateUrl: './order-create.component.html',
  styleUrls: ['./order-create.component.scss'],
  standalone: true,
  imports: [CommonModule, FormsModule],
})
export class OrderCreateComponent implements OnInit {
  // Mantém apenas o Observable; o async pipe no template faz subscribe/unsubscribe
  produtos$!: Observable<PageProductResponse>;
  filterTrigger$ = new BehaviorSubject<void>(undefined); // dispara nova busca
  error: string | null = null;

  cart: CartItem[] = [];
  loadingSubmit = false;

  // FILTROS e PAGINAÇÃO (caso queira implementar)
  filterName: string = '';
  filterCategory: string = '';
  filterMinPrice?: number;
  filterMaxPrice?: number;
  page = 0;
  size = 10;

  constructor(
    private productService: ProductService,
    private orderService: OrderService,
    private router: Router
  ) {}

  ngOnInit(): void {
    // produtos$ emite sempre que filterTrigger$ dispara
    this.produtos$ = this.filterTrigger$.pipe(
      // a cada emissão (inclusive a inicial), faz a chamada
      switchMap(() =>
        this.productService
          .list(
            this.filterName,
            this.filterCategory,
            this.filterMinPrice,
            this.filterMaxPrice,
            this.page,
            this.size
          )
          .pipe(
            catchError((_) => {
              this.error = 'Erro ao carregar produtos.';
              // retorna uma página vazia para não quebrar template
              return of({
                content: [],
                totalElements: 0,
                totalPages: 0,
                number: 0,
                size: this.size,
              });
            })
          )
      )
    );
  }

  // Chamado ao clicar “Pesquisar”
  onSearch(): void {
    this.page = 0; // volta para a página inicial
    this.error = null;
    this.filterTrigger$.next();
  }

  // Limpa filtros e recarrega
  clearFilters(): void {
    this.filterName = '';
    this.filterCategory = '';
    this.filterMinPrice = undefined;
    this.filterMaxPrice = undefined;
    this.page = 0;
    this.error = null;
    this.filterTrigger$.next();
  }

  // Paginação (volta/avança página)
  changePage(delta: number): void {
    this.page = this.page + delta;
    if (this.page < 0) this.page = 0;
    this.error = null;
    this.filterTrigger$.next();
  }

  // Adiciona item ao carrinho
  addToCart(product: Product, quantityInput: HTMLInputElement): void {
    const qty = parseInt(quantityInput.value, 10);
    if (!qty || qty <= 0 || qty > product.stock) {
      this.error = 'Quantidade inválida.';
      return;
    }
    this.error = null;
    const existing = this.cart.find((item) => item.product.id === product.id);
    if (existing) {
      existing.quantity += qty;
    } else {
      this.cart.push({ product, quantity: qty });
    }
    quantityInput.value = '';
  }

  // Remove item do carrinho
  removeFromCart(productId: number): void {
    this.cart = this.cart.filter((item) => item.product.id !== productId);
  }

  // Total do carrinho
  getCartTotal(): number {
    return this.cart.reduce(
      (acc, item) => acc + item.product.price * item.quantity,
      0
    );
  }

  // Submete o pedido
  submitOrder(): void {
    if (!this.cart.length) {
      this.error = 'O pedido deve conter ao menos um item.';
      return;
    }
    this.error = null;
    this.loadingSubmit = true;

    const items: OrderItemRequest[] = this.cart.map((item) => ({
      productId: item.product.id,
      quantity: item.quantity,
    }));
    const orderRequest: OrderRequest = { items };

    this.orderService.create(orderRequest).subscribe({
      next: () => {
        this.loadingSubmit = false;
        this.router.navigate(['/orders']);
      },
      error: (err) => {
        this.error = 'Erro ao criar pedido. ' + (err.error?.message || '');
        this.loadingSubmit = false;
      },
    });
  }
}
