import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { FormsModule } from '@angular/forms';
import { ClienteService } from '../../../core/services/cliente.service';
import { ClienteResponse } from '../../../core/models';
import { ConfirmDialogComponent, ConfirmDialogData } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-cliente-list',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    FormsModule,
    MatTableModule,
    MatIconModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatDialogModule,
    MatSnackBarModule,
    LoadingSpinnerComponent,
  ],
  template: `
    <div class="page-container">
      <div class="page-header">
        <h1>Clientes</h1>
        <button mat-raised-button color="primary" routerLink="/clientes/novo">
          <mat-icon>add</mat-icon>
          Novo Cliente
        </button>
      </div>

      <mat-card class="search-card">
        <mat-card-content>
          <div class="search-row">
            <mat-form-field appearance="outline" class="search-field">
              <mat-label>Buscar por nome</mat-label>
              <input matInput [(ngModel)]="searchTerm" (input)="onSearch()" placeholder="Digite o nome..." />
              <mat-icon matSuffix>search</mat-icon>
            </mat-form-field>
            <mat-form-field appearance="outline" class="search-field">
              <mat-label>Buscar por telefone</mat-label>
              <input matInput [(ngModel)]="searchPhone" (input)="onSearchPhone()" placeholder="Digite o telefone..." />
              <mat-icon matSuffix>phone</mat-icon>
            </mat-form-field>
          </div>
        </mat-card-content>
      </mat-card>

      @if (loading()) {
        <app-loading-spinner />
      } @else if (clientes().length === 0) {
        <div class="empty-state">
          <mat-icon>people_outline</mat-icon>
          <p>Nenhum cliente encontrado</p>
          <button mat-stroked-button color="primary" routerLink="/clientes/novo">
            Cadastrar primeiro cliente
          </button>
        </div>
      } @else {
        <mat-table [dataSource]="clientes()" class="mat-elevation-z2">
          <ng-container matColumnDef="id">
            <mat-header-cell *matHeaderCellDef>ID</mat-header-cell>
            <mat-cell *matCellDef="let cliente">{{ cliente.id }}</mat-cell>
          </ng-container>

          <ng-container matColumnDef="nome">
            <mat-header-cell *matHeaderCellDef>Nome</mat-header-cell>
            <mat-cell *matCellDef="let cliente">{{ cliente.nome }}</mat-cell>
          </ng-container>

          <ng-container matColumnDef="telefone">
            <mat-header-cell *matHeaderCellDef>Telefone</mat-header-cell>
            <mat-cell *matCellDef="let cliente">{{ cliente.telefone }}</mat-cell>
          </ng-container>

          <ng-container matColumnDef="acoes">
            <mat-header-cell *matHeaderCellDef>Ações</mat-header-cell>
            <mat-cell *matCellDef="let cliente">
              <button mat-icon-button color="primary" [routerLink]="['/clientes', cliente.id]">
                <mat-icon>edit</mat-icon>
              </button>
              <button mat-icon-button color="warn" (click)="confirmDelete(cliente)">
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
      .search-card {
        margin-bottom: 24px;
      }

      .search-row {
        display: flex;
        gap: 16px;
        flex-wrap: wrap;
      }

      .search-field {
        flex: 1;
        min-width: 200px;
      }

      .mat-table {
        border-radius: 8px;
        overflow: hidden;
      }

      .mat-column-acoes {
        width: 120px;
        text-align: center;
      }
    `,
  ],
})
export class ClienteListComponent implements OnInit {
  private clienteService = inject(ClienteService);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);

  loading = signal(true);
  clientes = signal<ClienteResponse[]>([]);
  searchTerm = '';
  searchPhone = '';
  displayedColumns = ['id', 'nome', 'telefone', 'acoes'];

  ngOnInit(): void {
    this.loadClientes();
  }

  loadClientes(): void {
    this.loading.set(true);
    this.clienteService.listarTodos().subscribe({
      next: (data) => {
        this.clientes.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.snackBar.open('Erro ao carregar clientes', 'Fechar', { duration: 3000 });
      },
    });
  }

  onSearch(): void {
    if (this.searchTerm.trim()) {
      this.loading.set(true);
      this.clienteService.buscarPorNome(this.searchTerm.trim()).subscribe({
        next: (cliente) => {
          this.clientes.set([cliente]);
          this.loading.set(false);
        },
        error: () => {
          this.clientes.set([]);
          this.loading.set(false);
        },
      });
    } else {
      this.loadClientes();
    }
  }

  onSearchPhone(): void {
    if (this.searchPhone.trim()) {
      this.loading.set(true);
      this.clienteService.buscarPorTelefone(this.searchPhone.trim()).subscribe({
        next: (cliente) => {
          this.clientes.set([cliente]);
          this.loading.set(false);
        },
        error: () => {
          this.clientes.set([]);
          this.loading.set(false);
        },
      });
    } else {
      this.loadClientes();
    }
  }

  confirmDelete(cliente: ClienteResponse): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Confirmar exclusão',
        message: `Deseja realmente excluir o cliente "${cliente.nome}"?`,
        confirmText: 'Excluir',
        cancelText: 'Cancelar',
      } as ConfirmDialogData,
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.clienteService.deletar(cliente.id).subscribe({
          next: () => {
            this.snackBar.open('Cliente excluído com sucesso', 'Fechar', { duration: 3000 });
            this.loadClientes();
          },
          error: () => {
            this.snackBar.open('Erro ao excluir cliente', 'Fechar', { duration: 3000 });
          },
        });
      }
    });
  }
}