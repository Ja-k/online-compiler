import { Routes } from '@angular/router';
import { HomeComponent } from './home-component/home.component';
import { LoginComponent } from './login-component/login.component';
import { RegisterComponent } from './register-component/register.component';
import { VerifyEmailComponent } from './verify-email-component/verify-email.component';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'verify-email', component: VerifyEmailComponent },
  { path: '**', redirectTo: '' }
];
