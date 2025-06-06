import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import {
  FormGroup,
  FormBuilder,
  Validators,
  ReactiveFormsModule,
} from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ProductRequest } from '../product.model';

@Component({
  selector: 'app-product-form',
  templateUrl: './product-form.component.html',
  styleUrls: ['./product-form.component.scss'],
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule],
})
export class ProductFormComponent implements OnInit {
  @Input() initialData?: ProductRequest;
  @Output() saved = new EventEmitter<ProductRequest>();

  productForm!: FormGroup;
  error: string | null = null;

  constructor(private fb: FormBuilder) {}

  ngOnInit(): void {
    this.productForm = this.fb.group({
      name: [
        this.initialData?.name ?? '',
        [Validators.required, Validators.maxLength(100)],
      ],
      description: [
        this.initialData?.description ?? '',
        Validators.maxLength(1000),
      ],
      price: [
        this.initialData?.price ?? 0,
        [Validators.required, Validators.min(0)],
      ],
      stock: [
        this.initialData?.stock ?? 0,
        [Validators.required, Validators.min(0)],
      ],
    });
  }

  onSubmit(): void {
    if (this.productForm.invalid) {
      return;
    }
    this.saved.emit(this.productForm.value);
  }
}
