import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ClienteService } from '../../../core/services/cliente.service';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-cliente-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
    LoadingSpinnerComponent,
  ],
  template: `
    <div class="page-container">
      <div class="page-header">
        <h1>{{ isEditing ? 'Editar Cliente' : 'Novo Cliente' }}</h1>
        <button mat-stroked-button routerLink="/clientes">
          <mat-icon>arrow_back</mat-icon>
          Voltar
        </button>
      </div>

      @if (loading()) {
        <app-loading-spinner />
      } @else {
        <mat-card>
          <mat-card-content>
            <form [formGroup]="clienteForm" (ngSubmit)="onSubmit()">
              <div class="form-row">
                <mat-form-field appearance="outline">
                  <mat-label>Nome</mat-label>
                  <input matInput formControlName="nome" placeholder="Nome do cliente" />
                  @if (clienteForm.get('nome')?.hasError('required') && clienteForm.get('nome')?.touched) {
                    <mat-error>Nome é obrigatório</mat-error>
                  }
                </mat-form-field>

                <mat-form-field appearance="outline">
                  <mat-label>Telefone</mat-label>
                  <input matInput formControlName="telefone" placeholder="(XX) XXXXX-XXXX" />
                  @if (clienteForm.get('telefone')?.hasError('required') && clienteForm.get('telefone')?.touched) {
                    <mat-error>Telefone é obrigatório</mat-error>
                  }
                </mat-form-field>
              </div>

              <div class="form-actions">
                <button mat-stroked-button type="button" routerLink="/clientes">Cancelar</button>
                <button mat-raised-button color="primary" type="submit" [disabled]="submitting">
                  @if (submitting) {
                    <mat-spinner diameter="20"></mat-spinner>
                  } @else {
                    {{ isEditing ? 'Atualizar' : 'Cadastrar' }}
                  }
                </button>
              </div>
            </form>
          </mat-card-content>
        </mat-card>
      }
    </div>
  `,
  styles: [
    `
      form {
        display: flex;
        flex-direction: column;
        gap: 16px;
      }

      button[type='submit'] {
        min-width: 140px;
      }

      mat-spinner {
        display: inline-block;
        margin: 0 auto;
      }
    `,
  ],
})
export class ClienteFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private clienteService = inject(ClienteService);
  private snackBar = inject(MatSnackBar);

  isEditing = false;
  editingId: number | null = null;
  loading = signal(false);
  submitting = false;

  clienteForm = this.fb.nonNullable.group({
    nome: ['', Validators.required],
    telefone: ['', Validators.required],
  });

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditing = true;
      this.editingId = Number(id);
      this.loadCliente();
    }
  }

  private loadCliente(): void {
    if (!this.editingId) return;
    this.loading.set(true);
    this.clienteService.buscarPorId(this.editingId).subscribe({
      next: (cliente) => {
        this.clienteForm.patchValue(cliente);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.snackBar.open('Erro ao carregar cliente', 'Fechar', { duration: 3000 });
        this.router.navigate(['/clientes']);
      },
    });
  }

  onSubmit(): void {
    if (this.clienteForm.invalid) return;
    this.submitting = true;
    const request = this.clienteForm.getRawValue();

    const action$ = this.isEditing && this.editingId
      ? this.clienteService.atualizar(this.editingId, request)
      : this.clienteService.cadastrar(request);

    action$.subscribe({
      next: () => {
        this.snackBar.open(
          this.isEditing ? 'Cliente atualizado com sucesso' : 'Cliente cadastrado com sucesso',
          'Fechar',
          { duration: 3000 }
        );
        this.router.navigate(['/clientes']);
      },
      error: (err) => {
        this.submitting = false;
        const message = err.error?.message || 'Erro ao salvar cliente';
        this.snackBar.open(message, 'Fechar', { duration: 5000 });
      },
    });
  }
}