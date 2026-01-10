/**
 * Vue 3 Composable for managing date formatting with locale support
 * Uses Pinia store for locale state management and localStorage persistence
 */

import { computed } from 'vue'
import { formatDate as utilFormatDate, getSystemLocale } from '~/utils/dateFormatter'
import { useMainStore } from '~/store'
import type { SupportedLocale } from '~/types/localization'

// Cache system locale detection
let cachedSystemLocale: SupportedLocale | null = null

export function useDateFormatting() {
  const store = useMainStore()

  // Get system locale (cached)
  if (!cachedSystemLocale) {
    cachedSystemLocale = getSystemLocale()
  }

  // Computed effective locale (user selected or system)
  const locale = computed(() => {
    if (store.userLocale === 'auto') {
      return cachedSystemLocale || getSystemLocale()
    }
    return store.userLocale
  })

  // Get the system locale (for reference in UI)
  const systemLocale = computed(() => cachedSystemLocale || getSystemLocale())

  /**
   * Format an ISO date string using current locale
   * Returns formatted string (e.g., "15:30" for today, "10/01" for other days)
   */
  const formatDate = (isoString: string | null | undefined): string => {
    const result = utilFormatDate(isoString, locale.value)
    return result.formattedDate
  }

  return {
    locale,
    systemLocale,
    formatDate,
  }
}
