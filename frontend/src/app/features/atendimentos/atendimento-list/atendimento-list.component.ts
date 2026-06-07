import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { AtendimentoService } from '../../../core/services/atendimento.service';
import { AtendimentoResponse } from '../../../core/models';
import { ConfirmDialogComponent, ConfirmDialogData } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-atendimento-list',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MatTableModule,
    MatIconModule,
    MatButtonModule,
    MatCardModule,
    MatChipsModule,
    MatDialogModule,
    MatSnackBarModule,
    LoadingSpinnerComponent,
  ],
  template: `
    <div class="page-container">
      <div class="page-header">
        <h1>Atendimentos</h1>
        <button mat-raised-button color="primary" routerLink="/atendimentos/novo">
          <mat-icon>add</mat-icon>
          Novo Atendimento
        </button>
      </div>

      @if (loading()) {
        <app-loading-spinner />
      } @else if (atendimentos().length === 0) {
        <div class="empty-state">
          <mat-icon>calendar_today</mat-icon>
          <p>Nenhum atendimento encontrado</p>
          <button mat-stroked-button color="primary" routerLink="/atendimentos/novo">
            Criar primeiro atendimento
          </button>
        </div>
      } @else {
        <mat-table [dataSource]="atendimentos()" class="mat-elevation-z2">
          <ng-container matColumnDef="id">
            <mat-header-cell *matHeaderCellDef>ID</mat-header-cell>
            <mat-cell *matCellDef="let att">{{ att.id }}</mat-cell>
          </ng-container>

          <ng-container matColumnDef="clienteNome">
            <mat-header-cell *matHeaderCellDef>Cliente</mat-header-cell>
            <mat-cell *matCellDef="let att">{{ att.clienteNome }}</mat-cell>
          </ng-container>

          <ng-container matColumnDef="profissionalNome">
            <mat-header-cell *matHeaderCellDef>Profissional</mat-header-cell>
            <mat-cell *matCellDef="let att">{{ att.profissionalNome }}</mat-cell>
          </ng-container>

          <ng-container matColumnDef="servicoEscolhido">
            <mat-header-cell *matHeaderCellDef>Serviço</mat-header-cell>
            <mat-cell *matCellDef="let att">
              <mat-chip>{{ att.servicoEscolhido }}</mat-chip>
            </mat-cell>
          </ng-container>

          <ng-container matColumnDef="preco">
            <mat-header-cell *matHeaderCellDef>Preço</mat-header-cell>
            <mat-cell *matCellDef="let att">{{ att.preco | currency:'BRL':'symbol':'1.2-2' }}</mat-cell>
          </ng-container>

          <ng-container matColumnDef="dataAtendimento">
            <mat-header-cell *matHeaderCellDef>Data</mat-header-cell>
            <mat-cell *matCellDef="let att">{{ att.dataAtendimento | date:'dd/MM/yyyy HH:mm' }}</mat-cell>
          </ng-container>

          <ng-container matColumnDef="acoes">
            <mat-header-cell *matHeaderCellDef>Ações</mat-header-cell>
            <mat-cell *matCellDef="let att">
              <button mat-icon-button color="primary" [routerLink]="['/atendimentos', att.id]">
                <mat-icon>edit</mat-icon>
              </button>
              <button mat-icon-button color="warn" (click)="confirmDelete(att)">
                <mat-icon>delete</mat-icon>
              </button>
            </mat-cell>
          </ng-container>

          <mat-header-row *matHeaderRowDef="displayedColumns"></mat-header-row>
          <mat-row *matRowDef="let row; columns: displayedColumns;"></mat-row>
        </mat-table>
      }
    </div>
  `,
  styles: [
    `
      .mat-column-acoes {
        width: 120px;
        text-align: center;
      }

      .mat-column-preco {
        width: 120px;
      }

      .mat-column-dataAtendimento {
        width: 160px;
      }

      .mat-column-servicoEscolhido {
        width: 150px;
      }
    `,
  ],
})
export class AtendimentoListComponent implements OnInit {
  private atendimentoService = inject(AtendimentoService);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);

  loading = signal(true);
  atendimentos = signal<AtendimentoResponse[]>([]);
  displayedColumns = ['id', 'clienteNome', 'profissionalNome', 'servicoEscolhido', 'preco', 'dataAtendimento', 'acoes'];

  ngOnInit(): void {
    this.loadAtendimentos();
  }

  loadAtendimentos(): void {
    this.loading.set(true);
    this.atendimentoService.listarTodos().subscribe({
      next: (data) => {
        this.atendimentos.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.snackBar.open('Erro ao carregar atendimentos', 'Fechar', { duration: 3000 });
      },
    });
  }

  confirmDelete(att: AtendimentoResponse): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Confirmar exclusão',
        message: `Deseja realmente excluir o atendimento #${att.id} do cliente "${att.clienteNome}"?`,
        confirmText: 'Excluir',
        cancelText: 'Cancelar',
      } as ConfirmDialogData,
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.atendimentoService.deletar(att.id).subscribe({
          next: () => {
            this.snackBar.open('Atendimento excluído com sucesso', 'Fechar', { duration: 3000 });
            this.loadAtendimentos();
          },
          error: () => {
            this.snackBar.open('Erro ao excluir atendimento', 'Fechar', { duration: 3000 });
          },
        });
      }
    });
  }
}