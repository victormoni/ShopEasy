import { Product } from '../products/product.model';

export interface CartItem {
  product: Product;
  quantity: number;
}

export interface OrderItemRequest {
  productId: number;
  quantity: number;
}

export interface OrderRequest {
  items: OrderItemRequest[];
}

export interface OrderItemResponse {
  id: number;
  productId: number;
  productName: string;
  quantity: number;
  unitPrice: number;
  total: number;
}

export interface OrderResponse {
  id: number;
  createdAt: string;
  total: number;
  items: OrderItemResponse[];
}
