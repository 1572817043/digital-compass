import { expect, test } from '@playwright/test';
import { collectPageErrors, expectImagesHealthy, expectReadableControls, loginAsAdmin } from './helpers';

test.describe('用户端冒烟测试', () => {
  test('公共页面可打开，控件和图片没有明显视觉异常', async ({ page }) => {
    const collector = collectPageErrors(page);
    const pages = [
      { path: '/', text: 'DigiCompass' },
      { path: '/products', text: '产品库' },
      { path: '/products/1', text: '关键指标' },
      { path: '/market', text: '价格行情' },
      { path: '/compare', text: '对比' },
    ];

    for (const item of pages) {
      await page.goto(item.path);
      await expect(page.locator('body')).toContainText(item.text);
      await expectReadableControls(page);
      await expectImagesHealthy(page);
    }

    collector.assertClean();
  });

  test('错误密码提示保持可见', async ({ page }) => {
    const collector = collectPageErrors(page, { ignoredConsoleText: [/401 \(Unauthorized\)/] });
    await page.goto('/login');
    await page.getByPlaceholder('请输入账号').fill('admin');
    await page.getByPlaceholder('请输入密码').fill('wrong-password');
    await page.getByRole('button', { name: '登录管理端' }).click();
    await expect(page.locator('.error-message')).toBeVisible();
    await expect(page.locator('.error-message')).toContainText(/账号|密码|错误|失败/);
    await expectReadableControls(page);
    collector.assertClean();
  });
});

test.describe('管理端冒烟测试', () => {
  test('产品资料弹窗图片维护区可读可用', async ({ page }) => {
    const collector = collectPageErrors(page);
    await loginAsAdmin(page);
    await page.goto('/admin/products');
    await expect(page.locator('body')).toContainText('产品管理');

    await page.getByRole('button', { name: '资料' }).first().click();
    await expect(page.getByRole('heading', { name: /资料维护/ })).toBeVisible();
    await page.getByRole('button', { name: '图片' }).click();

    const uploadButton = page.locator('label.upload-btn');
    await expect(uploadButton).toBeVisible();
    await expect(uploadButton).toContainText(/上传图片|上传中/);
    await expect(page.locator('.upload-area select')).toBeVisible();
    await expectReadableControls(page);
    await expectImagesHealthy(page);
    collector.assertClean();
  });
});
