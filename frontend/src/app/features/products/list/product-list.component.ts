import { Component, OnInit, OnDestroy } from '@angular/core';
import { ProductService } from '../product.service';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../features/auth/auth.service';
import { Subscription } from 'rxjs';
import { PageProductResponse, ProductResponse } from '../product.model';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-product-list',
  templateUrl: './product-list.component.html',
  styleUrls: ['./product-list.component.scss'],
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
})
export class ProductListComponent implements OnInit, OnDestroy {
  products: ProductResponse[] = [];
  loading = false;
  error: string | null = null;
  isAdmin = false;
  private subscriptions: Subscription[] = [];

  // filtros
  filterName: string = '';
  filterCategory: string = '';
  filterMinPrice: number | undefined = undefined;
  filterMaxPrice: number | undefined = undefined;

  // paginação
  page: number = 0;
  size: number = 10;
  totalElements: number = 0;
  totalPages: number = 0;

  constructor(
    private productService: ProductService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    const subRole = this.authService.isAdmin$.subscribe((isAdm) => {
      this.isAdmin = isAdm;
    });
    this.subscriptions.push(subRole);

    this.loadProducts();
  }

  loadProducts(): void {
    this.loading = true;
    this.error = null;

    this.productService
      .list(
        this.filterName,
        this.filterCategory,
        this.filterMinPrice,
        this.filterMaxPrice,
        this.page,
        this.size
      )
      .subscribe({
        next: (pageData: PageProductResponse) => {
          this.products = pageData.content;
          this.totalElements = pageData.totalElements;
          this.totalPages = pageData.totalPages;
          // pageData.number === this.page, pageData.size === this.size
          this.loading = false;
        },
        error: (err) => {
          console.error('Erro ao carregar produtos paginados:', err);
          this.error = 'Falha ao carregar produtos.';
          this.loading = false;
        },
      });
  }
  onSearch(): void {
    this.page = 0; // volta para a primeira página ao filtrar
    this.loadProducts();
  }

  /** limpa todos os filtros e recarrega desde a página zero */
  clearFilters(): void {
    this.filterName = '';
    this.filterCategory = '';
    this.filterMinPrice = undefined;
    this.filterMaxPrice = undefined;
    this.page = 0;
    this.loadProducts();
  }

  /** Muda de página em +delta (pode ser +1 ou -1) */
  changePage(delta: number): void {
    const newPage = this.page + delta;
    if (newPage < 0 || newPage >= this.totalPages) {
      return;
    }
    this.page = newPage;
    this.loadProducts();
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }
}
