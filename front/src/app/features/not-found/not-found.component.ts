import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

@Component({
  selector: 'app-not-found',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink],
  templateUrl: './not-found.component.html',
  styleUrl: './not-found.component.css'
})
export default class NotFoundComponent {
  private readonly router = inject(Router);

  protected readonly requestedUrl = computed(() => {
    const url = this.router.url;
    return url === '' ? '/' : url;
  });

  protected goBack(): void {
    if (window.history.length > 1) {
      window.history.back();
      return;
    }

    this.router.navigateByUrl('/');
  }
}
