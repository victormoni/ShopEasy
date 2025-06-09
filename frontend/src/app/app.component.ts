import { Component, OnInit } from '@angular/core';
import { AuthService } from './features/auth/auth.service';
import { Observable, of, switchMap } from 'rxjs';
import { RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import { HeaderComponent } from './core/components/header/header.component';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
  standalone: true,
  imports: [CommonModule, RouterOutlet, HeaderComponent],
})
export class AppComponent implements OnInit {
  isAdmin$!: Observable<boolean>;

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    this.isAdmin$ = this.authService.isLoggedIn$.pipe(
      switchMap((loggedIn) => {
        if (loggedIn) {
          return this.authService.isAdmin$;
        }
        return of(false);
      })
    );
  }
}
