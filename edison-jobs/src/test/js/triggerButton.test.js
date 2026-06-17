import { describe, it, expect, beforeEach, vi } from 'vitest'
import $ from 'jquery'

globalThis.$ = $
globalThis.jQuery = $
globalThis.window = globalThis
globalThis.window.__testing__ = true

// Replace location with a plain object so we can spy on assign without jsdom blocking it
const mockLocation = { assign: vi.fn() }
vi.stubGlobal('location', mockLocation)

const { initTriggerButtons } = await import('../../main/resources/static/internal/js/triggerButton.js')

describe('initTriggerButtons', () => {
    beforeEach(() => {
        document.body.innerHTML = `
            <button class="triggerButton" data-trigger-url="/internal/jobs/job-1/trigger"></button>
        `
        vi.clearAllMocks()
        initTriggerButtons()
    })

    it('POSTs to the trigger URL on click', () => {
        $.ajax = vi.fn()
        document.querySelector('.triggerButton').click()
        expect($.ajax).toHaveBeenCalledOnce()
        const args = $.ajax.mock.calls[0][0]
        expect(args.type).toBe('POST')
        expect(args.url).toBe('/internal/jobs/job-1/trigger')
    })

    it('redirects to Location header on success', () => {
        const xhrMock = { getResponseHeader: vi.fn(() => '/internal/jobs/job-1') }
        $.ajax = vi.fn(({ success }) => success({}, 'success', xhrMock))
        document.querySelector('.triggerButton').click()
        expect(mockLocation.assign).toHaveBeenCalledWith('/internal/jobs/job-1')
    })

    it('alerts Conflict message on 409', () => {
        globalThis.alert = vi.fn()
        $.ajax = vi.fn(({ error }) => error({}, 'error', 'Conflict'))
        document.querySelector('.triggerButton').click()
        expect(alert).toHaveBeenCalledWith('Job is currently running or blocked by a different job.')
    })

    it('alerts generic message on other errors', () => {
        globalThis.alert = vi.fn()
        $.ajax = vi.fn(({ error }) => error({}, 'error', 'Internal Server Error'))
        document.querySelector('.triggerButton').click()
        expect(alert).toHaveBeenCalledWith(expect.stringContaining('Internal Server Error'))
    })
})
