/**
 * Pure date formatting utilities with no Vue/Nuxt dependencies
 * Uses native Intl API for locale-aware formatting
 */

import type { FormattedDate, SupportedLocale } from '~/types/localization'

/**
 * Safely parse an ISO date string to a Date object
 * Returns null if parsing fails
 */
export function parseISODate(isoString: string | null | undefined): Date | null {
  if (!isoString) return null
  try {
    return new Date(isoString)
  } catch {
    return null
  }
}

/**
 * Check if a given date is today (same day, month, and year)
 */
export function isToday(date: Date): boolean {
  const today = new Date()
  return (
    date.getDate() === today.getDate() &&
    date.getMonth() === today.getMonth() &&
    date.getFullYear() === today.getFullYear()
  )
}

/**
 * Detect the browser's system locale
 * Falls back to 'en-US' if detection fails
 */
export function getSystemLocale(): SupportedLocale {
  const browserLocale = navigator.language || 'en-US'

  // Map common locale codes to supported locales
  const localeMap: Record<string, SupportedLocale> = {
    'en-US': 'en-US',
    'en-GB': 'en-GB',
    'en': 'en-US', // Default English to US
    'de': 'de-DE',
    'de-DE': 'de-DE',
    'fr': 'fr-FR',
    'fr-FR': 'fr-FR',
    'pt-BR': 'pt-BR',
    'pt': 'pt-BR', // Default Portuguese to Brazil
    'es': 'es-ES',
    'es-ES': 'es-ES',
    'it': 'it-IT',
    'it-IT': 'it-IT',
    'ja': 'ja-JP',
    'ja-JP': 'ja-JP',
    'zh-CN': 'zh-CN',
    'zh': 'zh-CN',
  }

  // Try exact match first
  if (localeMap[browserLocale]) {
    return localeMap[browserLocale]
  }

  // Try language code (first part before hyphen)
  const languageCode = browserLocale.split('-')[0]
  if (localeMap[languageCode]) {
    return localeMap[languageCode]
  }

  // Default fallback
  return 'en-US'
}

/**
 * Format a time part (hours and minutes) based on locale
 * For today's messages, we show HH:MM in 24-hour format
 */
function formatTimeOnly(date: Date): string {
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${hours}:${minutes}`
}

/**
 * Format a date part (day and month) using Intl API
 * Locale determines DD/MM vs MM/DD format
 */
function formatDateOnly(date: Date, locale: SupportedLocale | string): string {
  try {
    const formatter = new Intl.DateTimeFormat(locale, {
      day: '2-digit',
      month: '2-digit',
    })
    return formatter.format(date)
  } catch {
    // Fallback if locale not supported
    const day = String(date.getDate()).padStart(2, '0')
    const month = String(date.getMonth() + 1).padStart(2, '0')
    return `${day}/${month}`
  }
}

/**
 * Main date formatting function
 * For today's dates: returns HH:MM format
 * For other dates: returns locale-aware DD/MM or MM/DD format
 */
export function formatDate(
  isoString: string | null | undefined,
  locale: SupportedLocale | string,
): FormattedDate {
  const date = parseISODate(isoString)

  if (!date) {
    return {
      formattedDate: '—',
      isToday: false,
    }
  }

  const _isToday = isToday(date)

  return {
    formattedDate: _isToday ? formatTimeOnly(date) : formatDateOnly(date, locale),
    isToday: _isToday,
  }
}

/**
 * Format just the date part (ignoring whether it's today or not)
 * Useful for full date display
 */
export function formatDateFull(
  isoString: string | null | undefined,
  locale: SupportedLocale | string,
): string {
  const date = parseISODate(isoString)
  if (!date) return '—'

  try {
    const formatter = new Intl.DateTimeFormat(locale, {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
    })
    return formatter.format(date)
  } catch {
    // Fallback
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hours = String(date.getHours()).padStart(2, '0')
    const minutes = String(date.getMinutes()).padStart(2, '0')
    return `${day}/${month}/${year} ${hours}:${minutes}`
  }
}
