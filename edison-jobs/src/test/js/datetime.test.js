import { describe, it, expect, beforeEach } from 'vitest'
import { formatUTCToLocalDateTime, formatUTCToLocalTime, formatInitialDates } from '../../main/resources/static/internal/js/datetime.js'
import $ from 'jquery'

globalThis.$ = $
globalThis.jQuery = $

describe('formatUTCToLocalDateTime', () => {
    it('returns "-" for null', () => {
        expect(formatUTCToLocalDateTime(null)).toBe('-')
    })

    it('returns "-" for empty string', () => {
        expect(formatUTCToLocalDateTime('')).toBe('-')
    })

    it('returns a formatted datetime string for a valid ISO string', () => {
        const result = formatUTCToLocalDateTime('2024-06-01T12:00:00Z')
        // format: YYYY-MM-DD HH:MM:SS  (19 chars)
        expect(result).toMatch(/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$/)
    })
})

describe('formatUTCToLocalTime', () => {
    it('returns "-" for null', () => {
        expect(formatUTCToLocalTime(null)).toBe('-')
    })

    it('returns "-" for empty string', () => {
        expect(formatUTCToLocalTime('')).toBe('-')
    })

    it('returns only the time portion (HH:MM:SS)', () => {
        const result = formatUTCToLocalTime('2024-06-01T12:00:00Z')
        expect(result).toMatch(/^\d{2}:\d{2}:\d{2}$/)
    })
})

describe('formatInitialDates', () => {
    beforeEach(() => {
        document.body.innerHTML = `
            <span class="job-started" data-datetime="2024-06-01T08:00:00Z"></span>
            <span class="job-stopped" data-datetime="2024-06-01T09:00:00Z"></span>
            <span class="job-updated" data-datetime=""></span>
            <span class="job-messagedate" data-datetime="2024-06-01T08:30:00Z"></span>
        `
    })

    it('fills date-time nodes with formatted local datetime', () => {
        formatInitialDates()
        const started = document.querySelector('.job-started').textContent
        expect(started).toMatch(/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$/)
    })

    it('fills empty datetime nodes with "-"', () => {
        formatInitialDates()
        const stopped = document.querySelector('.job-updated').textContent
        expect(stopped).toBe('-')
    })

    it('fills message-date nodes with bracketed time', () => {
        formatInitialDates()
        const msgDate = document.querySelector('.job-messagedate').textContent
        expect(msgDate).toMatch(/^\[\d{2}:\d{2}:\d{2}\]$/)
    })
})
