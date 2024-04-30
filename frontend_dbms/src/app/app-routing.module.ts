import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {LoginComponent} from "./features/user/login/login.component";
import {HomeComponent} from "./pages/home/home.component";
import {AuthGuard} from "./core/guards/auth.guard";
import {RegistrationComponent} from "./features/user/registration/registration.component";
import {MainContentComponent} from "./pages/main-content/main-content.component";
import {BookDetailComponent} from "./features/book/book-detail/book-detail.component";
import {CartComponent} from "./features/cart/cart.component";
import {ReadingProgressListComponent} from "./features/readingprogresslist/readingprogresslist.component";

const routes: Routes = [
  { path: '', redirectTo: '/home', pathMatch: 'full' },
  { path: 'register', component: RegistrationComponent},
  { path: 'book/:id', component: BookDetailComponent },
  { path: 'login', component: LoginComponent },
  { path: 'cart', component: CartComponent },
  {path: 'reading-progress', component: ReadingProgressListComponent},
  { path: 'main-content', component: MainContentComponent},
  // { path: 'home', component: HomeComponent, canActivate: [AuthGuard]}
  { path: 'home', component: HomeComponent}

];


@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
