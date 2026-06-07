import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { ClienteService } from '../../core/services/cliente.service';
import { ProfissionalService } from '../../core/services/profissional.service';
import { AtendimentoService } from '../../core/services/atendimento.service';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MatCardModule,
    MatIconModule,
    MatButtonModule,
    LoadingSpinnerComponent,
  ],
  template: `
    <div class="page-container">
      <div class="page-header">
        <h1>Dashboard</h1>
      </div>

      @if (loading()) {
        <app-loading-spinner />
      } @else {
        <div class="stats-grid">
          <mat-card class="stat-card clients" routerLink="/clientes">
            <mat-card-content>
              <div class="stat-header">
                <mat-icon class="stat-icon">people</mat-icon>
                <span class="stat-value">{{ stats().clientes }}</span>
              </div>
              <p class="stat-label">Clientes</p>
            </mat-card-content>
          </mat-card>

          <mat-card class="stat-card professionals" routerLink="/profissionais">
            <mat-card-content>
              <div class="stat-header">
                <mat-icon class="stat-icon">badge</mat-icon>
                <span class="stat-value">{{ stats().profissionais }}</span>
              </div>
              <p class="stat-label">Profissionais</p>
            </mat-card-content>
          </mat-card>

          <mat-card class="stat-card appointments" routerLink="/atendimentos">
            <mat-card-content>
              <div class="stat-header">
                <mat-icon class="stat-icon">calendar_today</mat-icon>
                <span class="stat-value">{{ stats().atendimentos }}</span>
              </div>
              <p class="stat-label">Atendimentos</p>
            </mat-card-content>
          </mat-card>
        </div>

        <div class="quick-actions">
          <h2>Ações Rápidas</h2>
          <div class="actions-grid">
            <button mat-raised-button color="primary" routerLink="/clientes/novo">
              <mat-icon>person_add</mat-icon>
              Novo Cliente
            </button>
            <button mat-raised-button color="accent" routerLink="/profissionais/novo">
              <mat-icon>person_add</mat-icon>
              Novo Profissional
            </button>
            <button mat-raised-button color="primary" routerLink="/atendimentos/novo">
              <mat-icon>post_add</mat-icon>
              Novo Atendimento
            </button>
          </div>
        </div>
      }
    </div>
  `,
  styles: [
    `
      .stats-grid {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
        gap: 24px;
        margin-bottom: 40px;
      }

      .stat-card {
        cursor: pointer;
        transition: transform 0.2s, box-shadow 0.2s;

        &:hover {
          transform: translateY(-4px);
          box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
        }

        &.clients {
          border-left: 4px solid #3f51b5;
        }

        &.professionals {
          border-left: 4px solid #ff4081;
        }

        &.appointments {
          border-left: 4px solid #4caf50;
        }
      }

      .stat-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 8px;
      }

      .stat-icon {
        font-size: 40px;
        width: 40px;
        height: 40px;
        opacity: 0.7;
      }

      .stat-value {
        font-size: 36px;
        font-weight: 500;
      }

      .stat-label {
        margin: 0;
        color: #666;
        font-size: 16px;
      }

      .quick-actions {
        h2 {
          font-size: 20px;
          font-weight: 500;
          margin-bottom: 16px;
        }
      }

      .actions-grid {
        display: flex;
        gap: 16px;
        flex-wrap: wrap;

        button {
          min-width: 200px;
          height: 48px;
        }
      }
    `,
  ],
})
export class DashboardComponent implements OnInit {
  private clienteService = inject(ClienteService);
  private profissionalService = inject(ProfissionalService);
  private atendimentoService = inject(AtendimentoService);

  loading = signal(true);
  stats = signal({ clientes: 0, profissionais: 0, atendimentos: 0 });

  ngOnInit(): void {
    this.loadStats();
  }

  private loadStats(): void {
    this.clienteService.listarTodos().subscribe((clientes) => {
      this.stats.update((s) => ({ ...s, clientes: clientes.length }));
    });

    this.profissionalService.listarTodos().subscribe((profissionais) => {
      this.stats.update((s) => ({ ...s, profissionais: profissionais.length }));
    });

    this.atendimentoService.listarTodos().subscribe((atendimentos) => {
      this.stats.update((s) => ({ ...s, atendimentos: atendimentos.length }));
      this.loading.set(false);
    });
  }
}