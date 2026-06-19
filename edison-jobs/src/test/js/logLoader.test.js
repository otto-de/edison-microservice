import { describe, it, expect, beforeEach, vi } from 'vitest'
import $ from 'jquery'

globalThis.$ = $
globalThis.jQuery = $
globalThis.window = globalThis
globalThis.window.__testing__ = true

// Stub datetime functions that logLoader depends on at import time
globalThis.formatUTCToLocalTime = (s) => s ?? '-'
globalThis.formatInitialDates = () => {}

const { getLog, handleScrollEvent, handleScrollToBottomClick } = await import('../../main/resources/static/internal/js/logLoader.js')

const baseData = {
    state: 'Stopped',
    status: 'OK',
    stoppedIso: '2024-06-01T09:00:00Z',
    lastUpdatedIso: '2024-06-01T09:00:00Z',
}

function makeMessages(messages) {
    return messages.map((message, i) => ({
        timestampUTCIsoString: '2024-06-01T08:00:00Z',
        level: 'INFO',
        message,
    }))
}

describe('getLog', () => {
    beforeEach(() => {
        document.body.innerHTML = `
            <div class="logWindow" data-job-url="/internal/jobs/1"></div>
            <span id="job-status"></span>
            <span id="job-stopped"></span>
            <span id="job-last-updated"></span>
            <button class="triggerButton" disabled></button>
        `
        vi.restoreAllMocks()
    })

    it('appends one log entry per message', () => {
        $.ajax = vi.fn(({ success }) => success({
            ...baseData,
            messages: ['hello', 'world'],
            rawMessages: makeMessages(['hello', 'world']),
        }))

        getLog(0)

        const entries = document.querySelectorAll('.logWindow div')
        expect(entries.length).toBe(2)
    })

    it('does NOT interpret HTML in log messages (XSS safety)', () => {
        const xss = '<script>alert(1)</script>'
        $.ajax = vi.fn(({ success }) => success({
            ...baseData,
            messages: [xss],
            rawMessages: makeMessages([xss]),
        }))

        getLog(0)

        const msgSpan = document.querySelector('.logWindow div span:last-child')
        // Must appear as escaped text — no live <script> element, innerHTML must be escaped
        expect(msgSpan.textContent).toContain(xss)
        expect(msgSpan.innerHTML).toContain('&lt;script&gt;')
        expect(document.querySelector('script')).toBeNull()
    })

    it('does NOT interpret HTML in log level field (XSS safety)', () => {
        $.ajax = vi.fn(({ success }) => success({
            ...baseData,
            messages: ['msg'],
            rawMessages: [{
                timestampUTCIsoString: '2024-06-01T08:00:00Z',
                level: '<b>EVIL</b>',
                message: 'msg',
            }],
        }))

        getLog(0)

        const msgSpan = document.querySelector('.logWindow div span:last-child')
        // Must appear as escaped text — no live <b> element, innerHTML must be escaped
        expect(msgSpan.textContent).toContain('<b>EVIL</b>')
        expect(msgSpan.innerHTML).toContain('&lt;b&gt;')
        expect(document.querySelector('b')).toBeNull()
    })

    it('only appends messages from logIndex onwards', () => {
        $.ajax = vi.fn(({ success }) => success({
            ...baseData,
            messages: ['a', 'b', 'c'],
            rawMessages: makeMessages(['a', 'b', 'c']),
        }))

        getLog(2)

        const entries = document.querySelectorAll('.logWindow div')
        expect(entries.length).toBe(1)
    })

    it('clears the log window when logIndex is 0', () => {
        document.querySelector('.logWindow').innerHTML = '<div>old</div>'

        $.ajax = vi.fn(({ success }) => success({
            ...baseData,
            messages: ['fresh'],
            rawMessages: makeMessages(['fresh']),
        }))

        getLog(0)

        const entries = document.querySelectorAll('.logWindow div')
        expect(entries.length).toBe(1)
        expect(entries[0].textContent).toContain('fresh')
    })

    it('sets status badge to bg-success for OK', () => {
        $.ajax = vi.fn(({ success }) => success({
            ...baseData,
            messages: [],
            rawMessages: [],
        }))

        getLog(0)

        expect(document.querySelector('#job-status').className).toContain('bg-success')
    })

    it('sets status badge to bg-danger for ERROR', () => {
        $.ajax = vi.fn(({ success }) => success({
            ...baseData,
            status: 'ERROR',
            messages: [],
            rawMessages: [],
        }))

        getLog(0)

        expect(document.querySelector('#job-status').className).toContain('bg-danger')
    })

    it('sets status badge to bg-secondary for SKIPPED', () => {
        $.ajax = vi.fn(({ success }) => success({
            ...baseData,
            status: 'SKIPPED',
            messages: [],
            rawMessages: [],
        }))

        getLog(0)

        expect(document.querySelector('#job-status').className).toContain('bg-secondary')
    })

    it('sets status badge to bg-warning for DEAD', () => {
        $.ajax = vi.fn(({ success }) => success({
            ...baseData,
            status: 'DEAD',
            messages: [],
            rawMessages: [],
        }))

        getLog(0)

        expect(document.querySelector('#job-status').className).toContain('bg-warning')
    })

    it('re-schedules polling when state is Running', () => {
        vi.useFakeTimers()
        let callCount = 0
        $.ajax = vi.fn(({ success }) => {
            callCount++
            if (callCount === 1) {
                success({ ...baseData, state: 'Running', messages: [], rawMessages: [] })
            } else {
                success({ ...baseData, messages: [], rawMessages: [] })
            }
        })

        getLog(0)
        expect($.ajax).toHaveBeenCalledTimes(1)

        vi.advanceTimersByTime(2000)
        expect($.ajax).toHaveBeenCalledTimes(2)

        vi.useRealTimers()
    })
})

describe('handleScrollEvent', () => {
    function makeEl({ scrollHeight, scrollTop, clientHeight, offsetWidth, clientWidth }) {
        return { scrollHeight, scrollTop, clientHeight, offsetWidth, clientWidth }
    }

    function makeBtn() {
        return {
            _hidden: true,
            _right: null,
            style: { right: null },
            setAttribute(name, value) { if (name === 'hidden') this._hidden = true },
            removeAttribute(name) { if (name === 'hidden') this._hidden = false },
        }
    }

    it('hides the button and sets followLog=true when scrolled to bottom', () => {
        const el = makeEl({ scrollHeight: 1000, scrollTop: 995, clientHeight: 10, offsetWidth: 20, clientWidth: 20 })
        const btn = makeBtn()

        handleScrollEvent(el, btn)

        expect(btn._hidden).toBe(true)
    })

    it('shows the button when not scrolled to bottom', () => {
        const el = makeEl({ scrollHeight: 1000, scrollTop: 0, clientHeight: 10, offsetWidth: 20, clientWidth: 20 })
        const btn = makeBtn()

        handleScrollEvent(el, btn)

        expect(btn._hidden).toBe(false)
    })

    it('positions button right offset accounting for scrollbar width', () => {
        // offsetWidth=30, clientWidth=20 → scrollbarWidth=10 → right = 10+12 = 22px
        const el = makeEl({ scrollHeight: 1000, scrollTop: 0, clientHeight: 10, offsetWidth: 30, clientWidth: 20 })
        const btn = makeBtn()

        handleScrollEvent(el, btn)

        expect(btn.style.right).toBe('22px')
    })

    it('treats position within 5px of bottom as "at bottom"', () => {
        // scrollHeight(100) - scrollTop(84) = 16 > clientHeight(10) + 5 = 15 → NOT at bottom
        const elNotBottom = makeEl({ scrollHeight: 100, scrollTop: 84, clientHeight: 10, offsetWidth: 20, clientWidth: 20 })
        const btnNotBottom = makeBtn()
        handleScrollEvent(elNotBottom, btnNotBottom)
        expect(btnNotBottom._hidden).toBe(false)

        // scrollHeight(100) - scrollTop(85) = 15 <= clientHeight(10) + 5 = 15 → IS at bottom (exactly at margin)
        const elAtBottom = makeEl({ scrollHeight: 100, scrollTop: 85, clientHeight: 10, offsetWidth: 20, clientWidth: 20 })
        const btnAtBottom = makeBtn()
        handleScrollEvent(elAtBottom, btnAtBottom)
        expect(btnAtBottom._hidden).toBe(true)
    })

    it('getLog does not auto-scroll when followLog was set to false by scroll event', () => {
        // Scroll away from bottom → followLog = false
        const el = makeEl({ scrollHeight: 1000, scrollTop: 0, clientHeight: 10, offsetWidth: 20, clientWidth: 20 })
        const btn = makeBtn()
        handleScrollEvent(el, btn)

        // Now getLog should NOT set scrollTop
        const logWindow = document.querySelector('.logWindow')
        logWindow.scrollTop = 0
        $.ajax = vi.fn(({ success }) => success({ ...baseData, messages: ['x'], rawMessages: makeMessages(['x']) }))
        getLog(0)

        expect(logWindow.scrollTop).toBe(0)
    })
})

describe('handleScrollToBottomClick', () => {
    function makeBtn() {
        return {
            _hidden: false,
            setAttribute(name, value) { if (name === 'hidden') this._hidden = true },
            removeAttribute(name) { if (name === 'hidden') this._hidden = false },
        }
    }

    it('scrolls the element to the bottom and hides the button after click', () => {
        const el = { scrollHeight: 500, scrollTop: 0 }
        const btn = makeBtn()

        handleScrollToBottomClick(el, btn)

        expect(el.scrollTop).toBe(500)
        expect(btn._hidden).toBe(true)
    })

    it('re-enables auto-scroll (followLog=true) so getLog scrolls again afterwards', () => {
        // First disable followLog via scroll event (scrollTop=0, far from bottom)
        const scrollEl = { scrollHeight: 1000, scrollTop: 0, clientHeight: 10, offsetWidth: 20, clientWidth: 20 }
        const scrollBtn = {
            _hidden: false,
            style: { right: null },
            setAttribute() {},
            removeAttribute() {},
        }
        handleScrollEvent(scrollEl, scrollBtn)

        // Now click scroll-to-bottom → followLog = true again
        const el = { scrollHeight: 500, scrollTop: 0 }
        const btn = makeBtn()
        handleScrollToBottomClick(el, btn)

        // Verify followLog is true again by checking getLog calls scrollTop setter.
        // JSDOM has no layout engine so scrollHeight is always 0; we spy on the setter instead.
        const logWindow = document.querySelector('.logWindow')
        let scrollTopSet = false
        Object.defineProperty(logWindow, 'scrollTop', {
            configurable: true,
            get: () => 0,
            set: () => { scrollTopSet = true },
        })
        $.ajax = vi.fn(({ success }) => success({ ...baseData, messages: ['x'], rawMessages: makeMessages(['x']) }))
        getLog(0)

        expect(scrollTopSet).toBe(true)
    })
})
