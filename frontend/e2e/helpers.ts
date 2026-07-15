import { expect, type Page } from '@playwright/test';

const adminUsername = process.env.E2E_ADMIN_USER ?? 'admin';
const adminPassword = process.env.E2E_ADMIN_PASSWORD ?? '123456';

type ErrorCollector = {
  errors: string[];
  assertClean: () => void;
};

type ErrorCollectorOptions = {
  ignoredConsoleText?: RegExp[];
};

export function collectPageErrors(page: Page, options: ErrorCollectorOptions = {}): ErrorCollector {
  const errors: string[] = [];
  page.on('console', (message) => {
    if (message.type() === 'error') {
      const text = message.text();
      const ignored = [
        /favicon/,
        /ResizeObserver/,
        ...(options.ignoredConsoleText ?? []),
      ];
      if (!ignored.some((pattern) => pattern.test(text))) {
        errors.push(text);
      }
    }
  });
  page.on('pageerror', (error) => errors.push(error.message));
  return {
    errors,
    assertClean: () => expect(errors, `浏览器控制台错误：\n${errors.join('\n')}`).toEqual([]),
  };
}

export async function loginAsAdmin(page: Page) {
  await page.goto('/login');
  await page.getByPlaceholder('请输入账号').fill(adminUsername);
  await page.getByPlaceholder('请输入密码').fill(adminPassword);
  await page.getByRole('button', { name: '登录管理端' }).click();
  await expect(page).toHaveURL(/\/admin\/dashboard/);
}

export async function expectReadableControls(page: Page) {
  const failures = await page.locator('button, a.primary-button, a.ghost-button, label.primary-button, label.ghost-button, .upload-btn').evaluateAll((elements) => {
    function parseRgb(value: string) {
      const match = value.match(/rgba?\((\d+),\s*(\d+),\s*(\d+)(?:,\s*([\d.]+))?\)/);
      if (!match) return null;
      const alpha = match[4] === undefined ? 1 : Number(match[4]);
      return { r: Number(match[1]), g: Number(match[2]), b: Number(match[3]), a: alpha };
    }

    function luminance(rgb: { r: number; g: number; b: number }) {
      const values = [rgb.r, rgb.g, rgb.b].map((channel) => {
        const normalized = channel / 255;
        return normalized <= 0.03928 ? normalized / 12.92 : Math.pow((normalized + 0.055) / 1.055, 2.4);
      });
      return values[0] * 0.2126 + values[1] * 0.7152 + values[2] * 0.0722;
    }

    function contrast(foreground: { r: number; g: number; b: number }, background: { r: number; g: number; b: number }) {
      const lighter = Math.max(luminance(foreground), luminance(background));
      const darker = Math.min(luminance(foreground), luminance(background));
      return (lighter + 0.05) / (darker + 0.05);
    }

    function visibleText(element: Element) {
      return (element.textContent ?? '').replace(/\s+/g, ' ').trim();
    }

    function backgroundFor(element: Element) {
      let current: Element | null = element;
      while (current) {
        const color = parseRgb(getComputedStyle(current).backgroundColor);
        if (color && color.a > 0) return color;
        current = current.parentElement;
      }
      return { r: 255, g: 255, b: 255, a: 1 };
    }

    return elements.flatMap((element) => {
      const rect = element.getBoundingClientRect();
      const style = getComputedStyle(element);
      const text = visibleText(element);
      if (!text || rect.width < 1 || rect.height < 1 || style.visibility === 'hidden' || style.display === 'none') return [];
      const foreground = parseRgb(style.color);
      const background = backgroundFor(element);
      if (!foreground) return [];
      const ratio = contrast(foreground, background);
      if (ratio >= 3) return [];
      return [`${text} contrast=${ratio.toFixed(2)} color=${style.color} background=${background.r},${background.g},${background.b}`];
    });
  });

  expect(failures, `存在文字不可读的按钮或控件：\n${failures.join('\n')}`).toEqual([]);
}

export async function expectImagesHealthy(page: Page) {
  await page.waitForFunction(() => {
    const visibleImages = Array.from(document.images).filter((image) => {
      const rect = image.getBoundingClientRect();
      return rect.width >= 16 && rect.height >= 16;
    });
    return visibleImages.every((image) => image.complete);
  }, undefined, { timeout: 8_000 }).catch(() => null);

  const imageIssues = await page.locator('img').evaluateAll((images) => images.flatMap((image) => {
    const element = image as HTMLImageElement;
    const rect = element.getBoundingClientRect();
    const src = element.currentSrc || element.src;
    if (!src || rect.width < 16 || rect.height < 16) return [];
    if (!element.complete || element.naturalWidth < 16 || element.naturalHeight < 16) {
      return [`图片加载异常：${src}`];
    }
    return [];
  }));
  expect(imageIssues, `页面存在加载失败图片：\n${imageIssues.join('\n')}`).toEqual([]);

  const productImageUrls = await page.locator('img').evaluateAll((images) => Array.from(new Set(images
    .map((image) => (image as HTMLImageElement).currentSrc || (image as HTMLImageElement).src)
    .filter((src) => src.includes('/products/') || src.includes('oss-cn-')))));

  const tinyImages: string[] = [];
  for (const url of productImageUrls) {
    const response = await page.request.head(url, { timeout: 10_000 }).catch(() => null);
    const length = response?.headers()['content-length'];
    if (response?.ok() && length && Number(length) > 0 && Number(length) < 800) {
      tinyImages.push(`${url} content-length=${length}`);
    }
  }

  expect(tinyImages, `疑似占位小图或错误图片：\n${tinyImages.join('\n')}`).toEqual([]);
}
