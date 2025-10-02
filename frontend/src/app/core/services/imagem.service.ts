import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpClient, HttpHeaders } from '@angular/common/http';

import { BaseHttpService } from './base-http.service';
import { API_CONFIG } from '../constants/api.constants';

@Injectable({
  providedIn: 'root'
})
export class ImagemService extends BaseHttpService {

  /**
   * Faz upload da imagem do evento
   * @param eventoId ID do evento
   * @param imagem Arquivo de imagem
   * @returns Observable com a URL da imagem salva
   */
  uploadImagemEvento(eventoId: number, imagem: File): Observable<string> {
    const formData = new FormData();
    formData.append('imagem', imagem);

    // Não adicionar Content-Type header, o browser adiciona automaticamente com boundary correto
    return this.http.post(
      `${API_CONFIG.baseUrl}${API_CONFIG.endpoints.eventos.byId(eventoId)}/imagem`,
      formData,
      { responseType: 'text' }
    );
  }

  /**
   * Remove a imagem do evento
   * @param eventoId ID do evento
   * @returns Observable void
   */
  removerImagemEvento(eventoId: number): Observable<any> {
    return this.delete<any>(`${API_CONFIG.endpoints.eventos.byId(eventoId)}/imagem`);
  }

  /**
   * Retorna a URL completa para exibir a imagem
   * @param imagemUrl URL relativa retornada pelo backend
   * @returns URL completa da imagem
   */
  getImagemUrl(imagemUrl: string): string {
    if (!imagemUrl) return '';

    // Se já for uma URL completa, retornar como está
    if (imagemUrl.startsWith('http://') || imagemUrl.startsWith('https://')) {
      return imagemUrl;
    }

    // Se a URL já começar com /api/, usar apenas o protocolo e host
    if (imagemUrl.startsWith('/api/')) {
      // Extrair apenas o protocolo e host de API_CONFIG.baseUrl (ex: http://localhost:8080)
      const baseUrlWithoutApi = API_CONFIG.baseUrl.replace('/api', '');
      return `${baseUrlWithoutApi}${imagemUrl}`;
    }

    // Se for URL relativa sem /api/, adicionar base URL completa
    return `${API_CONFIG.baseUrl}${imagemUrl}`;
  }

  /**
   * Valida se o arquivo é uma imagem válida
   * @param file Arquivo a ser validado
   * @returns Objeto com isValid e errorMessage
   */
  validarImagem(file: File): { isValid: boolean; errorMessage?: string } {
    const MAX_SIZE = 5 * 1024 * 1024; // 5MB
    const ALLOWED_TYPES = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp'];
    const ALLOWED_EXTENSIONS = ['.jpg', '.jpeg', '.png', '.webp'];

    // Validar tamanho
    if (file.size > MAX_SIZE) {
      return {
        isValid: false,
        errorMessage: `Imagem muito grande. Tamanho máximo: 5MB. Tamanho atual: ${(file.size / (1024 * 1024)).toFixed(2)}MB`
      };
    }

    // Validar tipo MIME
    if (!ALLOWED_TYPES.includes(file.type.toLowerCase())) {
      return {
        isValid: false,
        errorMessage: `Formato não permitido. Formatos aceitos: JPG, JPEG, PNG, WebP`
      };
    }

    // Validar extensão do nome do arquivo
    const fileName = file.name.toLowerCase();
    const hasValidExtension = ALLOWED_EXTENSIONS.some(ext => fileName.endsWith(ext));

    if (!hasValidExtension) {
      return {
        isValid: false,
        errorMessage: `Extensão de arquivo não permitida. Extensões aceitas: .jpg, .jpeg, .png, .webp`
      };
    }

    return { isValid: true };
  }
}
