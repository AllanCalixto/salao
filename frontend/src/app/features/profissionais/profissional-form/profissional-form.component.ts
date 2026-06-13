import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators, FormArray } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { ProfissionalService } from '../../../core/services/profissional.service';
import { Especialidade, ESPECIALIDADE_LABELS } from '../../../core/models';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';

interface DiaSemanaOption {
  value: number;
  label: string;
}

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
    MatCheckboxModule,
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

              <!-- Horários de Atendimento -->
              <mat-card class="disponibilidade-card">
                <mat-card-header>
                  <mat-card-title>Horários de Atendimento</mat-card-title>
                  <mat-card-subtitle>Defina os dias e horários em que o profissional estará disponível para agendamento</mat-card-subtitle>
                </mat-card-header>
                <mat-card-content>
                  <div formArrayName="disponibilidade">
                    @for (item of disponibilidadeControls; track $index; let i = $index) {
                      <div class="disponibilidade-row" [formGroupName]="i">
                        <mat-checkbox formControlName="selecionado"></mat-checkbox>
                        <span class="dia-label">{{ diasSemana[i].label }}</span>
                        <mat-form-field appearance="outline" class="time-field">
                          <mat-label>Início</mat-label>
                          <input matInput type="time" formControlName="horaInicio" />
                        </mat-form-field>
                        <mat-form-field appearance="outline" class="time-field">
                          <mat-label>Fim</mat-label>
                          <input matInput type="time" formControlName="horaFim" />
                        </mat-form-field>
                      </div>
                    }
                  </div>
                </mat-card-content>
              </mat-card>

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

      .disponibilidade-card {
        margin-top: 16px;
      }

      .disponibilidade-card mat-card-subtitle {
        font-size: 0.85rem;
      }

      .disponibilidade-row {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 8px 0;
        border-bottom: 1px solid #eee;
      }

      .disponibilidade-row:last-child {
        border-bottom: none;
      }

      .dia-label {
        min-width: 90px;
        font-weight: 500;
      }

      .time-field {
        width: 120px;
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

  readonly diasSemana: DiaSemanaOption[] = [
    { value: 0, label: 'Domingo' },
    { value: 1, label: 'Segunda' },
    { value: 2, label: 'Terça' },
    { value: 3, label: 'Quarta' },
    { value: 4, label: 'Quinta' },
    { value: 5, label: 'Sexta' },
    { value: 6, label: 'Sábado' },
  ];

  profissionalForm = this.fb.nonNullable.group({
    nome: ['', Validators.required],
    especialidade: ['' as Especialidade, Validators.required],
    servicosText: ['', Validators.required],
    disponibilidade: this.fb.array(
      this.diasSemana.map(() =>
        this.fb.group({
          selecionado: [false],
          horaInicio: ['08:00'],
          horaFim: ['18:00'],
        })
      )
    ),
  });

  get disponibilidadeControls() {
    return (this.profissionalForm.get('disponibilidade') as FormArray).controls;
  }

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

        if (prof.disponibilidade && prof.disponibilidade.length > 0) {
          const dispArray = this.profissionalForm.get('disponibilidade') as FormArray;
          for (const disp of prof.disponibilidade) {
            const index = this.diasSemana.findIndex(d => d.value === disp.diaSemana);
            if (index >= 0) {
              const group = dispArray.at(index);
              group.patchValue({
                selecionado: disp.ativo,
                horaInicio: disp.horaInicio.substring(0, 5),
                horaFim: disp.horaFim.substring(0, 5),
              });
            }
          }
        }

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

    const disponibilidade = formValue.disponibilidade
      .map((item, index) => ({
        diaSemana: this.diasSemana[index].value,
        horaInicio: item.selecionado ? item.horaInicio + ':00' : null,
        horaFim: item.selecionado ? item.horaFim + ':00' : null,
      }))
      .filter(item => item.horaInicio !== null)
      .map(item => ({
        diaSemana: item.diaSemana,
        horaInicio: item.horaInicio!,
        horaFim: item.horaFim!,
      }));

    const request = {
      nome: formValue.nome,
      especialidade: formValue.especialidade,
      servicos,
      disponibilidade: disponibilidade.length > 0 ? disponibilidade : undefined,
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