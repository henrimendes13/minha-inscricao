import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

import { API_CONFIG } from '../constants/api.constants';

export interface RequestOptions {
  headers?: HttpHeaders;
  params?: HttpParams;
}

@Injectable({
  providedIn: 'root'
})
export class BaseHttpService {

  constructor(protected http: HttpClient) { }

  protected get<T>(endpoint: string, options?: RequestOptions): Observable<T> {
    return this.http.get<T>(`${API_CONFIG.baseUrl}${endpoint}`, options);
  }

  protected post<T>(endpoint: string, data: any, options?: RequestOptions): Observable<T> {
    return this.http.post<T>(`${API_CONFIG.baseUrl}${endpoint}`, data, options);
  }

  protected put<T>(endpoint: string, data: any, options?: RequestOptions): Observable<T> {
    return this.http.put<T>(`${API_CONFIG.baseUrl}${endpoint}`, data, options);
  }

  protected patch<T>(endpoint: string, data: any, options?: RequestOptions): Observable<T> {
    return this.http.patch<T>(`${API_CONFIG.baseUrl}${endpoint}`, data, options);
  }

  protected delete<T>(endpoint: string, options?: RequestOptions): Observable<T> {
    return this.http.delete<T>(`${API_CONFIG.baseUrl}${endpoint}`, options);
  }

  protected buildParams(params: Record<string, any>): HttpParams {
    let httpParams = new HttpParams();
    
    Object.keys(params).forEach(key => {
      if (params[key] !== null && params[key] !== undefined) {
        if (Array.isArray(params[key])) {
          params[key].forEach((value: any) => {
            httpParams = httpParams.append(key, value.toString());
          });
        } else {
          httpParams = httpParams.set(key, params[key].toString());
        }
      }
    });

    return httpParams;
  }
}
