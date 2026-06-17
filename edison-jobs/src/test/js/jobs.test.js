import { describe, it, expect, beforeEach, vi } from 'vitest'
import $ from 'jquery'

globalThis.$ = $
globalThis.jQuery = $
globalThis.window = globalThis
globalThis.window.__testing__ = true

globalThis.formatUTCToLocalDateTime = (s) => s ?? '-'
globalThis.formatInitialDates = () => {}

const { update } = await import('../../main/resources/static/internal/js/jobs.js')

const makeRow = (overrides = {}) => ({
    id: 'job-1',
    state: 'Stopped',
    status: 'OK',
    startedIso: '2024-06-01T08:00:00Z',
    stoppedIso: '2024-06-01T09:00:00Z',
    runtime: '1h',
    lastUpdated: '2024-06-01T09:00:00Z',
    ...overrides,
})

describe('update', () => {
    beforeEach(() => {
        document.body.innerHTML = `
            <div id="jobsContainer" data-jobs-url="/internal/jobs" data-type-filter=""></div>
            <span id="job-status-job-1" class="badge"></span>
            <button id="trigger-button-job-1" disabled></button>
            <span id="job-started-job-1"></span>
            <span id="job-stopped-job-1"></span>
            <span id="job-runtime-job-1"></span>
            <span id="job-last-updated-job-1"></span>
        `
        vi.restoreAllMocks()
    })

    it('sets bg-success badge for OK status', () => {
        $.ajax = vi.fn(({ success }) => success([makeRow({ status: 'OK' })]))
        update()
        expect(document.querySelector('#job-status-job-1').className).toContain('bg-success')
    })

    it('sets bg-danger badge for ERROR status', () => {
        $.ajax = vi.fn(({ success }) => success([makeRow({ status: 'ERROR' })]))
        update()
        expect(document.querySelector('#job-status-job-1').className).toContain('bg-danger')
    })

    it('sets bg-secondary badge for SKIPPED status', () => {
        $.ajax = vi.fn(({ success }) => success([makeRow({ status: 'SKIPPED' })]))
        update()
        expect(document.querySelector('#job-status-job-1').className).toContain('bg-secondary')
    })

    it('sets bg-warning badge for DEAD status', () => {
        $.ajax = vi.fn(({ success }) => success([makeRow({ status: 'DEAD' })]))
        update()
        expect(document.querySelector('#job-status-job-1').className).toContain('bg-warning')
    })

    it('enables the trigger button when job is not Running', () => {
        $.ajax = vi.fn(({ success }) => success([makeRow()]))
        update()
        expect(document.querySelector('#trigger-button-job-1').disabled).toBe(false)
    })

    it('does not update badge or enable button when state is Running', () => {
        $.ajax = vi.fn(({ success }) => success([makeRow({ state: 'Running' })]))
        update()
        // badge class should not have been changed to a status class
        expect(document.querySelector('#job-status-job-1').className).toBe('badge')
        expect(document.querySelector('#trigger-button-job-1').disabled).toBe(true)
    })

    it('fills in runtime and timestamps', () => {
        $.ajax = vi.fn(({ success }) => success([makeRow()]))
        update()
        expect(document.querySelector('#job-runtime-job-1').textContent).toBe('1h')
    })

    it('reschedules after 4000ms on success', () => {
        vi.useFakeTimers()
        $.ajax = vi.fn(({ success }) => success([makeRow()]))
        update()
        expect($.ajax).toHaveBeenCalledTimes(1)
        vi.advanceTimersByTime(4000)
        expect($.ajax).toHaveBeenCalledTimes(2)
        vi.useRealTimers()
    })

    it('reschedules after 10000ms on error', () => {
        vi.useFakeTimers()
        $.ajax = vi.fn(({ error }) => error({}, 'error', 'Internal Server Error'))
        update()
        expect($.ajax).toHaveBeenCalledTimes(1)
        vi.advanceTimersByTime(10000)
        expect($.ajax).toHaveBeenCalledTimes(2)
        vi.useRealTimers()
    })

    it('includes type filter in URL when set', () => {
        document.querySelector('#jobsContainer').setAttribute('data-type-filter', 'myType')
        $.ajax = vi.fn(({ success }) => success([]))
        update()
        const calledUrl = $.ajax.mock.calls[0][0].url
        expect(calledUrl).toContain('type=myType')
    })

    it('omits type filter from URL when empty', () => {
        $.ajax = vi.fn(({ success }) => success([]))
        update()
        const calledUrl = $.ajax.mock.calls[0][0].url
        expect(calledUrl).not.toContain('type=')
    })
})
