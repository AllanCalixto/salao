import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
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
import { MatDividerModule } from '@angular/material/divider';
import { AgendamentoService } from '../../core/services/agendamento.service';
import {
  ProfissionalResponse,
  ServicoResponse,
  DisponibilidadeResponse,
  DisponibilidadeProfissionalResponse,
} from '../../core/models';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';

// Tipo unificado para exibir serviço: pode vir do catálogo ou direto do profissional
interface ServicoOption {
  id: number | null;     // null quando não encontrado no catálogo
  nome: string;
  duracaoMinutos: number;
  preco: number;
  origem: 'catalogo' | 'profissional';
}

@Component({
  selector: 'app-agendamento-publico',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
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
    MatDividerModule,
    LoadingSpinnerComponent,
  ],
  template: `
    <div class="public-container">
      <mat-card class="public-card">
        <mat-card-header>
          <mat-card-title>
            <mat-icon class="title-icon">spa</mat-icon>
            Agende seu Horário
          </mat-card-title>
          <mat-card-subtitle>Preencha os dados abaixo para agendar seu atendimento</mat-card-subtitle>
        </mat-card-header>

        <mat-card-content>
          <form [formGroup]="agendamentoForm" (ngSubmit)="onSubmit()">
            <!-- Profissional -->
            <section class="form-section">
              <h3><mat-icon>person</mat-icon> Escolha o Profissional</h3>
              <mat-form-field appearance="outline" class="full-width">
                <mat-label>Profissional</mat-label>
                <mat-select formControlName="profissionalId" (selectionChange)="onProfissionalChange()">
                  @for (prof of profissionais; track prof.id) {
                    <mat-option [value]="prof.id">
                      {{ prof.nome }} - {{ getEspecialidadeLabel(prof.especialidade) }}
                    </mat-option>
                  }
                </mat-select>
                @if (agendamentoForm.get('profissionalId')?.hasError('required') && agendamentoForm.get('profissionalId')?.touched) {
                  <mat-error>Selecione um profissional</mat-error>
                }
              </mat-form-field>
            </section>

            <!-- Serviço -->
            <section class="form-section">
              <h3><mat-icon>content_cut</mat-icon> Escolha o Serviço</h3>
              <mat-form-field appearance="outline" class="full-width">
                <mat-label>Serviço</mat-label>
                <mat-select formControlName="servicoNome" (selectionChange)="onServicoChange()">
                  @for (servico of servicosDisponiveis; track $index) {
                    <mat-option [value]="servico.nome">
                      {{ servico.nome }}
                      @if (servico.duracaoMinutos > 0) {
                        <span class="servico-detalhe"> ({{ servico.duracaoMinutos }}min)</span>
                      }
                    </mat-option>
                  }
                </mat-select>
                @if (agendamentoForm.get('servicoNome')?.hasError('required') && agendamentoForm.get('servicoNome')?.touched) {
                  <mat-error>Selecione um serviço</mat-error>
                }
              </mat-form-field>
            </section>

            <!-- Data (com filtro de dias disponiveis) -->
            <section class="form-section">
              <h3><mat-icon>calendar_today</mat-icon> Escolha a Data</h3>
              <mat-form-field appearance="outline" class="full-width">
                <mat-label>Data do Agendamento</mat-label>
                <input matInput [matDatepicker]="picker" formControlName="data"
                       (dateChange)="onDataChange()" [min]="today"
                       [matDatepickerFilter]="filtroDiasDisponiveis">
                <mat-datepicker-toggle matSuffix [for]="picker"></mat-datepicker-toggle>
                <mat-datepicker #picker></mat-datepicker>
                @if (agendamentoForm.get('data')?.hasError('required') && agendamentoForm.get('data')?.touched) {
                  <mat-error>Selecione uma data</mat-error>
                }
                @if (diasDisponiveis().length === 0 && agendamentoForm.get('profissionalId')?.value) {
                  <mat-hint>Este profissional não possui dias disponíveis configurados</mat-hint>
                }
              </mat-form-field>
            </section>

            <!-- Horários -->
            @if (carregandoHorarios()) {
              <div class="loading-horarios">
                <mat-spinner diameter="24"></mat-spinner>
                <span>Carregando horários...</span>
              </div>
            }

            @if (disponibilidade() && !carregandoHorarios()) {
              <section class="form-section">
                <h3><mat-icon>schedule</mat-icon> Escolha o Horário</h3>

                @if (disponibilidade()!.horariosDisponiveis.length === 0) {
                  <div class="no-slots">
                    <mat-icon>event_busy</mat-icon>
                    <p>Não há horários disponíveis para esta data.</p>
                    <p class="hint">Escolha outra data ou profissional.</p>
                  </div>
                } @else {
                  <mat-form-field appearance="outline" class="full-width">
                    <mat-label>Horário</mat-label>
                    <mat-select formControlName="horario">
                      @for (slot of disponibilidade()!.horariosDisponiveis; track slot.horario) {
                        <mat-option [value]="slot.horario">
                          {{ slot.horarioFormatado }}
                        </mat-option>
                      }
                    </mat-select>
                    @if (agendamentoForm.get('horario')?.hasError('required') && agendamentoForm.get('horario')?.touched) {
                      <mat-error>Selecione um horário</mat-error>
                    }
                  </mat-form-field>
                  <p class="slot-info">
                    Duração: <strong>{{ disponibilidade()!.duracaoMinutos }} minutos</strong>
                  </p>
                }
              </section>
            }

            <!-- Dados do Cliente -->
            <mat-divider class="section-divider"></mat-divider>

            <section class="form-section">
              <h3><mat-icon>person</mat-icon> Seus Dados</h3>
              <div class="form-row">
                <mat-form-field appearance="outline">
                  <mat-label>Nome Completo</mat-label>
                  <input matInput formControlName="nomeCliente" placeholder="Digite seu nome">
                  @if (agendamentoForm.get('nomeCliente')?.hasError('required') && agendamentoForm.get('nomeCliente')?.touched) {
                    <mat-error>Nome é obrigatório</mat-error>
                  }
                </mat-form-field>

                <mat-form-field appearance="outline">
                  <mat-label>Telefone Celular</mat-label>
                  <input matInput formControlName="telefoneCliente" placeholder="(XX) XXXXX-XXXX">
                  @if (agendamentoForm.get('telefoneCliente')?.hasError('required') && agendamentoForm.get('telefoneCliente')?.touched) {
                    <mat-error>Telefone é obrigatório</mat-error>
                  }
                  @if (agendamentoForm.get('telefoneCliente')?.hasError('minlength')) {
                    <mat-error>Telefone inválido</mat-error>
                  }
                </mat-form-field>
              </div>
            </section>

            <!-- Mensagens -->
            @if (errorMessage()) {
              <div class="error-message">
                <mat-icon>error</mat-icon>
                <span>{{ errorMessage() }}</span>
              </div>
            }

            @if (sucesso()) {
              <div class="success-message">
                <mat-icon>check_circle</mat-icon>
                <div>
                  <p><strong>Agendamento confirmado!</strong></p>
                  <p>Protocolo: #{{ sucesso()!.id }}</p>
                  <p>Profissional: {{ sucesso()!.profissionalNome }}</p>
                  <p>Serviço: {{ sucesso()!.servicoNome }}</p>
                  <p>Data: {{ sucesso()!.dataInicio | date:'dd/MM/yyyy HH:mm' }} às {{ sucesso()!.dataFim | date:'HH:mm' }}</p>
                </div>
              </div>
            }

            @if (!sucesso()) {
              <div class="form-actions">
                <button mat-stroked-button type="button" (click)="limparFormulario()">
                  <mat-icon>clear</mat-icon>
                  Limpar
                </button>
                <button mat-raised-button color="primary" type="submit"
                        [disabled]="submitting || agendamentoForm.invalid">
                  @if (submitting) {
                    <mat-spinner diameter="20"></mat-spinner>
                  } @else {
                    <mat-icon>check</mat-icon>
                    Confirmar Agendamento
                  }
                </button>
              </div>
            }

            @if (sucesso()) {
              <div class="form-actions">
                <button mat-raised-button color="primary" type="button" (click)="limparFormulario()">
                  <mat-icon>add</mat-icon>
                  Novo Agendamento
                </button>
              </div>
            }
          </form>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [
    `
      .public-container {
        min-height: 100vh;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        padding: 24px 16px;
        display: flex;
        justify-content: center;
        align-items: flex-start;
      }

      .public-card {
        max-width: 720px;
        width: 100%;
        border-radius: 16px;
        box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);

        mat-card-header {
          text-align: center;
          padding: 32px 24px 16px;
          display: flex;
          flex-direction: column;
          align-items: center;

          .mat-card-title {
            font-size: 28px;
            font-weight: 500;
            display: flex;
            align-items: center;
            gap: 8px;
          }

          .title-icon {
            font-size: 36px;
            width: 36px;
            height: 36px;
            color: var(--primary-color);
          }

          .mat-card-subtitle {
            font-size: 16px;
            margin-top: 4px;
          }
        }

        mat-card-content {
          padding: 24px;
        }
      }

      .servico-detalhe {
        font-size: 12px;
        color: #888;
      }

      .form-section {
        margin-bottom: 24px;

        h3 {
          display: flex;
          align-items: center;
          gap: 8px;
          font-size: 16px;
          font-weight: 500;
          color: #555;
          margin: 0 0 12px;

          mat-icon {
            font-size: 20px;
            width: 20px;
            height: 20px;
            color: var(--primary-color);
          }
        }
      }

      .section-divider {
        margin: 24px 0;
      }

      .form-row {
        display: flex;
        gap: 16px;
        flex-wrap: wrap;

        mat-form-field {
          flex: 1;
          min-width: 200px;
        }
      }

      .loading-horarios {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 16px;
        background: #f5f5f5;
        border-radius: 8px;
        margin-bottom: 16px;
        color: #666;
      }

      .no-slots {
        text-align: center;
        padding: 24px;
        background: #fff3e0;
        border-radius: 8px;
        color: #e65100;

        mat-icon {
          font-size: 40px;
          width: 40px;
          height: 40px;
          margin-bottom: 8px;
        }

        p {
          margin: 4px 0;
          font-size: 15px;
        }

        .hint {
          font-size: 13px;
          opacity: 0.8;
        }
      }

      .slot-info {
        font-size: 13px;
        color: #666;
        margin: 8px 0 0;
        text-align: right;
      }

      .error-message {
        display: flex;
        align-items: center;
        gap: 8px;
        background: #fce4ec;
        color: #c62828;
        padding: 12px 16px;
        border-radius: 8px;
        margin-bottom: 16px;
        font-size: 14px;

        mat-icon {
          font-size: 20px;
          width: 20px;
          height: 20px;
        }
      }

      .success-message {
        display: flex;
        align-items: flex-start;
        gap: 12px;
        background: #e8f5e9;
        color: #2e7d32;
        padding: 16px;
        border-radius: 8px;
        margin-bottom: 16px;

        mat-icon {
          font-size: 36px;
          width: 36px;
          height: 36px;
          color: #4caf50;
        }

        p {
          margin: 2px 0;
          font-size: 14px;
        }
      }

      .form-actions {
        display: flex;
        justify-content: flex-end;
        gap: 12px;
        margin-top: 24px;
        padding-top: 16px;
        border-top: 1px solid #e0e0e0;

        button {
          min-width: 200px;
          height: 48px;
        }
      }

      @media (max-width: 600px) {
        .public-card mat-card-header {
          padding: 24px 16px 8px;

          .mat-card-title {
            font-size: 22px;
          }
        }

        mat-card-content {
          padding: 16px;
        }

        .form-actions button {
          min-width: 140px;
        }
      }
    `,
  ],
})
export class AgendamentoPublicoComponent implements OnInit {
  private fb = inject(FormBuilder);
  private router = inject(Router);
  private agendamentoService = inject(AgendamentoService);
  private snackBar = inject(MatSnackBar);

  hoje = new Date();
  today = new Date(this.hoje.getFullYear(), this.hoje.getMonth(), this.hoje.getDate());

  profissionais: ProfissionalResponse[] = [];
  servicosCatalogo: ServicoResponse[] = [];
  servicosDisponiveis: ServicoOption[] = [];

  disponibilidade = signal<DisponibilidadeResponse | null>(null);
  carregandoHorarios = signal(false);
  submitting = false;
  errorMessage = signal('');
  sucesso = signal<any>(null);

  // Dias da semana em que o profissional trabalha (0=Dom, 1=Seg... 6=Sab)
  diasDisponiveis = signal<number[]>([]);

  // Filtro para o datepicker - só permite dias que o profissional trabalha
  filtroDiasDisponiveis = (d: Date | null): boolean => {
    if (!d) return false;
    const dias = this.diasDisponiveis();
    if (dias.length === 0) return true; // sem restrição até profissional ser selecionado
    // DayOfWeek no JS: 0=Dom, 1=Seg... 6=Sab. Nosso formato: 0=Dom...6=Sab
    return dias.includes(d.getDay());
  };

  agendamentoForm = this.fb.nonNullable.group({
    profissionalId: [0 as number, Validators.required],
    servicoNome: ['', Validators.required],
    data: ['', Validators.required],
    horario: ['', Validators.required],
    nomeCliente: ['', Validators.required],
    telefoneCliente: ['', [Validators.required, Validators.minLength(10)]],
  });

  ngOnInit(): void {
    this.carregarDadosIniciais();
  }

  private carregarDadosIniciais(): void {
    this.agendamentoService.listarProfissionais().subscribe({
      next: (data) => (this.profissionais = data),
      error: () => this.snackBar.open('Erro ao carregar profissionais', 'Fechar', { duration: 3000 }),
    });

    this.agendamentoService.listarServicos().subscribe({
      next: (data) => {
        this.servicosCatalogo = data;
      },
      error: () => this.snackBar.open('Erro ao carregar serviços', 'Fechar', { duration: 3000 }),
    });
  }

  getEspecialidadeLabel(especialidade: string): string {
    const labels: Record<string, string> = {
      MANICURE: 'Manicure',
      CABELEREIRA: 'Cabeleireira',
      DEPILADORA: 'Depiladora',
    };
    return labels[especialidade] || especialidade;
  }

  onProfissionalChange(): void {
    const profId = this.agendamentoForm.get('profissionalId')?.value;
    const profissional = this.profissionais.find((p) => p.id === profId);

    if (profissional) {
      // Monta lista de serviços: tenta casar com catálogo, senão usa o nome do profissional
      this.servicosDisponiveis = profissional.servicos.map((nomeServico) => {
        const servicoCatalogo = this.servicosCatalogo.find(
          (s) => s.nome.toUpperCase() === nomeServico.toUpperCase()
        );
        if (servicoCatalogo) {
          return {
            id: servicoCatalogo.id,
            nome: servicoCatalogo.nome,
            duracaoMinutos: servicoCatalogo.duracaoMinutos,
            preco: servicoCatalogo.preco,
            origem: 'catalogo' as const,
          };
        }
        // Serviço não encontrado no catálogo, mas existe no profissional
        return {
          id: null,
          nome: nomeServico,
          duracaoMinutos: 30, // fallback padrão
          preco: 0,
          origem: 'profissional' as const,
        };
      });

      // Carrega os dias disponiveis do profissional
      this.agendamentoService.listarDisponibilidadeProfissional(profId!).subscribe({
        next: (dias: DisponibilidadeProfissionalResponse[]) => {
          const diasAtivos = dias.filter(d => d.ativo).map(d => d.diaSemana);
          this.diasDisponiveis.set(diasAtivos);
        },
        error: () => {
          this.diasDisponiveis.set([]);
        }
      });
    } else {
      this.servicosDisponiveis = [];
      this.diasDisponiveis.set([]);
    }

    this.agendamentoForm.patchValue({ servicoNome: '', horario: '' });
    this.disponibilidade.set(null);
  }

  onServicoChange(): void {
    this.agendamentoForm.patchValue({ horario: '' });
    const data = this.agendamentoForm.get('data')?.value;
    if (data) {
      this.carregarDisponibilidade();
    }
  }

  onDataChange(): void {
    this.agendamentoForm.patchValue({ horario: '' });
    const servicoNome = this.agendamentoForm.get('servicoNome')?.value;
    if (servicoNome) {
      this.carregarDisponibilidade();
    }
  }

  private carregarDisponibilidade(): void {
    const profId = this.agendamentoForm.get('profissionalId')?.value;
    const servicoNome = this.agendamentoForm.get('servicoNome')?.value;
    const data = this.agendamentoForm.get('data')?.value;

    if (!profId || !servicoNome || !data) return;

    this.carregandoHorarios.set(true);
    this.disponibilidade.set(null);

    const dataFormatada = this.formatDate(data);

    this.agendamentoService
      .consultarDisponibilidade(profId, servicoNome, dataFormatada)
      .subscribe({
        next: (response) => {
          this.disponibilidade.set(response);
          this.carregandoHorarios.set(false);
        },
        error: (err) => {
          this.carregandoHorarios.set(false);
          const msg = err.error || 'Erro ao consultar disponibilidade';
          this.errorMessage.set(typeof msg === 'string' ? msg : 'Erro ao carregar horários');
        },
      });
  }

  private formatDate(date: any): string {
    if (typeof date === 'string') {
      return date.split('T')[0];
    }
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  onSubmit(): void {
    if (this.agendamentoForm.invalid) return;

    this.submitting = true;
    this.errorMessage.set('');

    const formValue = this.agendamentoForm.getRawValue();
    const dataFormatada = this.formatDate(formValue.data);
    const servicoNome = formValue.servicoNome;

    // Busca o ID do serviço no catálogo
    const servicoOption = this.servicosDisponiveis.find((s) => s.nome === servicoNome);
    let servicoId: number;

    if (servicoOption && servicoOption.origem === 'catalogo' && servicoOption.id) {
      servicoId = servicoOption.id;
    } else {
      // Serviço só existe no profissional, não no catálogo - busca por nome
      const catalogo = this.servicosCatalogo.find(
        (s) => s.nome.toUpperCase() === servicoNome.toUpperCase()
      );
      servicoId = catalogo ? catalogo.id : 1; // fallback para ID 1 se não achar
    }

    const request = {
      nomeCliente: formValue.nomeCliente,
      telefoneCliente: formValue.telefoneCliente.replace(/\D/g, ''),
      profissionalId: formValue.profissionalId,
      servicoId: servicoId,
      servicoNome: servicoNome,
      data: dataFormatada,
      horario: formValue.horario,
    };

    this.agendamentoService.criarAgendamento(request).subscribe({
      next: (response) => {
        this.submitting = false;
        this.sucesso.set(response);
        this.snackBar.open('Agendamento realizado com sucesso!', 'Fechar', {
          duration: 5000,
        });
      },
      error: (err) => {
        this.submitting = false;
        const msg =
          typeof err.error === 'string'
            ? err.error
            : err.error?.message || 'Erro ao realizar agendamento. Tente novamente.';
        this.errorMessage.set(msg);
      },
    });
  }

  limparFormulario(): void {
    this.agendamentoForm.reset({
      profissionalId: 0,
      servicoNome: '',
      data: '',
      horario: '',
      nomeCliente: '',
      telefoneCliente: '',
    });
    this.servicosDisponiveis = [];
    this.disponibilidade.set(null);
    this.errorMessage.set('');
    this.sucesso.set(null);
  }
}