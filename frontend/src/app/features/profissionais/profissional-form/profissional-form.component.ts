import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ProfissionalService } from '../../../core/services/profissional.service';
import { Especialidade, ESPECIALIDADE_LABELS } from '../../../core/models';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-profissional-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
    LoadingSpinnerComponent,
  ],
  template: `
    <div class="page-container">
      <div class="page-header">
        <h1>{{ isEditing ? 'Editar Profissional' : 'Novo Profissional' }}</h1>
        <button mat-stroked-button routerLink="/profissionais">
          <mat-icon>arrow_back</mat-icon>
          Voltar
        </button>
      </div>

      @if (loading()) {
        <app-loading-spinner />
      } @else {
        <mat-card>
          <mat-card-content>
            <form [formGroup]="profissionalForm" (ngSubmit)="onSubmit()">
              <div class="form-row">
                <mat-form-field appearance="outline">
                  <mat-label>Nome</mat-label>
                  <input matInput formControlName="nome" placeholder="Nome do profissional" />
                  @if (profissionalForm.get('nome')?.hasError('required') && profissionalForm.get('nome')?.touched) {
                    <mat-error>Nome é obrigatório</mat-error>
                  }
                </mat-form-field>

                <mat-form-field appearance="outline">
                  <mat-label>Especialidade</mat-label>
                  <mat-select formControlName="especialidade">
                    @for (esp of especialidades; track esp) {
                      <mat-option [value]="esp">{{ ESPECIALIDADE_LABELS[esp] }}</mat-option>
                    }
                  </mat-select>
                  @if (profissionalForm.get('especialidade')?.hasError('required') && profissionalForm.get('especialidade')?.touched) {
                    <mat-error>Especialidade é obrigatória</mat-error>
                  }
                </mat-form-field>
              </div>

              <mat-form-field appearance="outline" class="full-width">
                <mat-label>Serviços (um por linha)</mat-label>
                <textarea matInput formControlName="servicosText" rows="4" placeholder="Exemplo:&#10;CORTE DE CABELO&#10;ESCOVA&#10;HIDRATAÇÃO"></textarea>
                @if (profissionalForm.get('servicosText')?.hasError('required') && profissionalForm.get('servicosText')?.touched) {
                  <mat-error>Pelo menos um serviço é obrigatório</mat-error>
                }
              </mat-form-field>

              <div class="form-actions">
                <button mat-stroked-button type="button" routerLink="/profissionais">Cancelar</button>
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
export class ProfissionalFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private profissionalService = inject(ProfissionalService);
  private snackBar = inject(MatSnackBar);

  readonly ESPECIALIDADE_LABELS = ESPECIALIDADE_LABELS;
  especialidades = Object.values(Especialidade);
  isEditing = false;
  editingId: number | null = null;
  loading = signal(false);
  submitting = false;

  profissionalForm = this.fb.nonNullable.group({
    nome: ['', Validators.required],
    especialidade: ['' as Especialidade, Validators.required],
    servicosText: ['', Validators.required],
  });

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditing = true;
      this.editingId = Number(id);
      this.loadProfissional();
    }
  }

  private loadProfissional(): void {
    if (!this.editingId) return;
    this.loading.set(true);
    this.profissionalService.buscarPorId(this.editingId).subscribe({
      next: (prof) => {
        this.profissionalForm.patchValue({
          nome: prof.nome,
          especialidade: prof.especialidade,
          servicosText: prof.servicos.join('\n'),
        });
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.snackBar.open('Erro ao carregar profissional', 'Fechar', { duration: 3000 });
        this.router.navigate(['/profissionais']);
      },
    });
  }

  onSubmit(): void {
    if (this.profissionalForm.invalid) return;
    this.submitting = true;

    const formValue = this.profissionalForm.getRawValue();
    const servicos = formValue.servicosText
      .split('\n')
      .map((s) => s.trim().toUpperCase())
      .filter((s) => s.length > 0);

    const request = {
      nome: formValue.nome,
      especialidade: formValue.especialidade,
      servicos,
    };

    const action$ = this.isEditing && this.editingId
      ? this.profissionalService.atualizar(this.editingId, request)
      : this.profissionalService.cadastrar(request);

    action$.subscribe({
      next: () => {
        this.snackBar.open(
          this.isEditing ? 'Profissional atualizado com sucesso' : 'Profissional cadastrado com sucesso',
          'Fechar',
          { duration: 3000 }
        );
        this.router.navigate(['/profissionais']);
      },
      error: (err) => {
        this.submitting = false;
        const message = err.error?.message || 'Erro ao salvar profissional';
        this.snackBar.open(message, 'Fechar', { duration: 5000 });
      },
    });
  }
}