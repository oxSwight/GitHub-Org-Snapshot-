import { expect, vi } from 'vitest'
import * as matchers from '@testing-library/jest-dom/matchers'

if (expect && typeof expect.extend === 'function') {
  expect.extend(matchers)
}
