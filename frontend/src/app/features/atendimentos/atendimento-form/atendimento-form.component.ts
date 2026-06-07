import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AtendimentoService } from '../../../core/services/atendimento.service';
import { ClienteService } from '../../../core/services/cliente.service';
import { ProfissionalService } from '../../../core/services/profissional.service';
import { ClienteResponse, ProfissionalResponse } from '../../../core/models';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-atendimento-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatButtonModule,
    MatIconModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
    LoadingSpinnerComponent,
  ],
  template: `
    <div class="page-container">
      <div class="page-header">
        <h1>{{ isEditing ? 'Editar Atendimento' : 'Novo Atendimento' }}</h1>
        <button mat-stroked-button routerLink="/atendimentos">
          <mat-icon>arrow_back</mat-icon>
          Voltar
        </button>
      </div>

      @if (loading()) {
        <app-loading-spinner />
      } @else {
        <mat-card>
          <mat-card-content>
            <form [formGroup]="atendimentoForm" (ngSubmit)="onSubmit()">
              <div class="form-row">
                <mat-form-field appearance="outline">
                  <mat-label>Cliente</mat-label>
                  <mat-select formControlName="clienteId">
                    @for (cliente of clientes; track cliente.id) {
                      <mat-option [value]="cliente.id">{{ cliente.nome }}</mat-option>
                    }
                  </mat-select>
                  @if (atendimentoForm.get('clienteId')?.hasError('required') && atendimentoForm.get('clienteId')?.touched) {
                    <mat-error>Cliente é obrigatório</mat-error>
                  }
                </mat-form-field>

                <mat-form-field appearance="outline">
                  <mat-label>Profissional</mat-label>
                  <mat-select formControlName="profissionalId" (selectionChange)="onProfissionalChange($event.value)">
                    @for (prof of profissionais; track prof.id) {
                      <mat-option [value]="prof.id">{{ prof.nome }} - {{ prof.especialidade }}</mat-option>
                    }
                  </mat-select>
                  @if (atendimentoForm.get('profissionalId')?.hasError('required') && atendimentoForm.get('profissionalId')?.touched) {
                    <mat-error>Profissional é obrigatório</mat-error>
                  }
                </mat-form-field>
              </div>

              <div class="form-row">
                <mat-form-field appearance="outline">
                  <mat-label>Serviço</mat-label>
                  <mat-select formControlName="servicoEscolhido">
                    @for (servico of servicosDisponiveis; track servico) {
                      <mat-option [value]="servico">{{ servico }}</mat-option>
                    }
                  </mat-select>
                  @if (atendimentoForm.get('servicoEscolhido')?.hasError('required') && atendimentoForm.get('servicoEscolhido')?.touched) {
                    <mat-error>Serviço é obrigatório</mat-error>
                  }
                </mat-form-field>

                <mat-form-field appearance="outline">
                  <mat-label>Preço</mat-label>
                  <input matInput type="number" formControlName="preco" placeholder="0,00" step="0.01" />
                  <span matTextSuffix>R$</span>
                  @if (atendimentoForm.get('preco')?.hasError('required') && atendimentoForm.get('preco')?.touched) {
                    <mat-error>Preço é obrigatório</mat-error>
                  }
                </mat-form-field>
              </div>

              <mat-form-field appearance="outline" class="full-width">
                <mat-label>Data do Atendimento</mat-label>
                <input matInput [matDatepicker]="picker" formControlName="dataAtendimento" />
                <mat-datepicker-toggle matSuffix [for]="picker"></mat-datepicker-toggle>
                <mat-datepicker #picker></mat-datepicker>
                @if (atendimentoForm.get('dataAtendimento')?.hasError('required') && atendimentoForm.get('dataAtendimento')?.touched) {
                  <mat-error>Data é obrigatória</mat-error>
                }
              </mat-form-field>

              <div class="form-actions">
                <button mat-stroked-button type="button" routerLink="/atendimentos">Cancelar</button>
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
export class AtendimentoFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private atendimentoService = inject(AtendimentoService);
  private clienteService = inject(ClienteService);
  private profissionalService = inject(ProfissionalService);
  private snackBar = inject(MatSnackBar);

  isEditing = false;
  editingId: number | null = null;
  loading = signal(false);
  submitting = false;
  clientes: ClienteResponse[] = [];
  profissionais: ProfissionalResponse[] = [];
  servicosDisponiveis: string[] = [];

  atendimentoForm = this.fb.nonNullable.group({
    clienteId: [0 as number, Validators.required],
    profissionalId: [0 as number, Validators.required],
    servicoEscolhido: ['', Validators.required],
    preco: [0 as number, Validators.required],
    dataAtendimento: ['' as string, Validators.required],
  });

  ngOnInit(): void {
    this.loadClientes();
    this.loadProfissionais();

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditing = true;
      this.editingId = Number(id);
      this.loadAtendimento();
    }
  }

  private loadClientes(): void {
    this.clienteService.listarTodos().subscribe({
      next: (data) => (this.clientes = data),
    });
  }

  private loadProfissionais(): void {
    this.profissionalService.listarTodos().subscribe({
      next: (data) => (this.profissionais = data),
    });
  }

  onProfissionalChange(profissionalId: number): void {
    const profissional = this.profissionais.find((p) => p.id === profissionalId);
    this.servicosDisponiveis = profissional?.servicos || [];
    this.atendimentoForm.patchValue({ servicoEscolhido: '' });
  }

  private loadAtendimento(): void {
    if (!this.editingId) return;
    this.loading.set(true);
    this.atendimentoService.buscarPorId(this.editingId).subscribe({
      next: (att) => {
        // Format date for input
        const date = new Date(att.dataAtendimento);
        const formattedDate = date.toISOString().split('T')[0];

        // Set services for this profissional
        const prof = this.profissionais.find((p) => p.nome === att.profissionalNome);
        if (prof) {
          this.servicosDisponiveis = prof.servicos;
          this.atendimentoForm.patchValue({
            clienteId: this.clientes.find((c) => c.nome === att.clienteNome)?.id || 0,
            profissionalId: prof.id,
            servicoEscolhido: att.servicoEscolhido,
            preco: att.preco,
            dataAtendimento: formattedDate,
          });
        }
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.snackBar.open('Erro ao carregar atendimento', 'Fechar', { duration: 3000 });
        this.router.navigate(['/atendimentos']);
      },
    });
  }

  onSubmit(): void {
    if (this.atendimentoForm.invalid) return;
    this.submitting = true;

    const formValue = this.atendimentoForm.getRawValue();
    const dataAtendimento = new Date(formValue.dataAtendimento);

    const dataInicio = dataAtendimento.toISOString();
    const dataFim = new Date(dataAtendimento.getTime() + 30 * 60 * 1000).toISOString();

    const request = {
      clienteId: formValue.clienteId,
      profissionalId: formValue.profissionalId,
      servicoEscolhido: formValue.servicoEscolhido,
      preco: formValue.preco,
      dataAtendimento: dataInicio,
      dataFim: dataFim,
    };

    const action$ = this.isEditing && this.editingId
      ? this.atendimentoService.atualizar(this.editingId, request)
      : this.atendimentoService.cadastrar(request);

    action$.subscribe({
      next: () => {
        this.snackBar.open(
          this.isEditing ? 'Atendimento atualizado com sucesso' : 'Atendimento cadastrado com sucesso',
          'Fechar',
          { duration: 3000 }
        );
        this.router.navigate(['/atendimentos']);
      },
      error: (err) => {
        this.submitting = false;
        const message = err.error?.message || 'Erro ao salvar atendimento';
        this.snackBar.open(message, 'Fechar', { duration: 5000 });
      },
    });
  }
}