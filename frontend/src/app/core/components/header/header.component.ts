import { Component, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../features/auth/auth.service';
import { Observable, combineLatest } from 'rxjs';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss'],
  standalone: true,
  imports: [CommonModule, RouterLink],
})
export class HeaderComponent implements OnInit {
  loggedIn$!: Observable<boolean>;
  isAdmin$!: Observable<boolean>;

  constructor(private authService: AuthService, private router: Router) {}

  ngOnInit(): void {
    this.loggedIn$ = this.authService.isLoggedIn$;
    this.isAdmin$ = this.authService.isAdmin$;
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
