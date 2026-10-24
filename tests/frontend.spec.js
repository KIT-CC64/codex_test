const { test, expect } = require('@playwright/test');
test('offline assets, four tabs, climate updates and safe-area layout', async ({ page }) => {
  const errors=[]; page.on('pageerror',e=>errors.push(e.message));
  const external=[]; page.on('request',r=>{if(!r.url().startsWith('http://127.0.0.1:8765'))external.push(r.url());});
  await page.goto('/');
  await expect(page.locator('.nav-btn')).toHaveCount(4);
  await expect(page.locator('#nav-review')).toHaveCount(1);
  await page.selectOption('#location-select','学生食堂');
  await expect(page.locator('#indoor-temp')).toHaveText('26.5');
  await expect(page.locator('#recommend-context')).toContainText('26.5');
  await page.evaluate(()=>ClimaFit.setClimate({outdoorTemp:28,indoorData:{'情報講義室A':{temp:28,humidity:60},'図書館 2F':{temp:22,humidity:50},'学生食堂':{temp:29,humidity:60},'実験室3':{temp:19,humidity:40}}}));
  await expect(page.locator('#recommend-title')).toHaveText('通気性の良い薄手の服装');
  for(const tab of ['review','logs','reviews','dashboard']) {await page.click('#nav-'+tab);await expect(page.locator('#tab-'+tab)).toBeVisible();}
  expect(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth)).toBe(true);
  expect(external).toEqual([]);expect(errors).toEqual([]);
});
test('clothing management removed and reviews safely escaped',async({page})=>{
 await page.goto('/');await expect(page.locator('#nav-closet')).toHaveCount(0);
 await expect(page.locator('#tab-closet')).toHaveCount(0);await expect(page.locator('#clothes-form')).toHaveCount(0);
 await page.click('#nav-reviews');await page.fill('#review-comment','<img src=x onerror=alert(1)>');
 await page.getByRole('button',{name:'口コミを投稿する'}).click();
 await expect(page.locator('#reviews-list img')).toHaveCount(0);
 await expect(page.locator('#reviews-list')).toContainText('<img src=x onerror=alert(1)>');
 await page.reload();await page.click('#nav-reviews');await expect(page.locator('#reviews-list')).toContainText('<img src=x onerror=alert(1)>');
});
test('save suggestion, filter logs and retain reviews',async({page})=>{
  await page.goto('/');await page.getByRole('button',{name:'この服装提案を記録する'}).click();
  await expect(page.locator('#logs-container article')).toHaveCount(1);
  await page.selectOption('#logs-mode','custom');await page.fill('#logs-temp','10');await expect(page.locator('#logs-container article')).toHaveCount(0);
  await page.fill('#logs-temp','24.2');await expect(page.locator('#logs-container article')).toHaveCount(1);
  await page.click('#nav-reviews');await page.fill('#review-comment','テスト口コミ');await page.getByRole('button',{name:'口コミを投稿する'}).click();
  await page.reload();await page.click('#nav-reviews');await expect(page.locator('#reviews-list')).toContainText('テスト口コミ');
  await page.click('#nav-logs');await expect(page.locator('#logs-container article')).toHaveCount(1);
});

test('AI tab previews photos without fabricating a diagnosis',async({page})=>{
 await page.goto('/'); await page.click('#nav-review');
 await expect(page.locator('#analyze-btn')).toBeDisabled();
 await page.locator('#camera-input').setInputFiles({name:'outfit.png',mimeType:'image/png',buffer:Buffer.from('iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+a0uoAAAAASUVORK5CYII=','base64')});
 await expect(page.locator('#image-preview-container')).toBeVisible();
 await expect(page.locator('#analyze-btn')).toBeEnabled(); await page.click('#analyze-btn');
 await expect(page.locator('#notice')).toContainText('写真は送信していません');
 await expect(page.locator('#ai-result-card')).toBeHidden();
 await page.locator('#image-preview-container button').click();
 await expect(page.locator('#analyze-btn')).toBeDisabled();
});
