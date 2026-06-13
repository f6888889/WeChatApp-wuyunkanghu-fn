from PIL import Image, ImageDraw, ImageFont
import os

ICONS_DIR = r'c:\Users\21166\Desktop\dachuang trea\frontend\assets\icons'
os.makedirs(ICONS_DIR, exist_ok=True)

COLOR_NORMAL = '#8B7D7B'
COLOR_ACTIVE = '#E07A5F'
SIZE = 81

def draw_icon(filename, draw_func):
    img = Image.new('RGBA', (SIZE, SIZE), (255, 255, 255, 0))
    draw = ImageDraw.Draw(img)
    draw_func(draw)
    img.save(os.path.join(ICONS_DIR, filename), 'PNG')

def draw_home(draw):
    draw.polygon([(40, 10), (10, 40), (20, 40), (20, 70), (60, 70), (60, 40), (70, 40)], fill=COLOR_NORMAL)

def draw_home_active(draw):
    draw.polygon([(40, 10), (10, 40), (20, 40), (20, 70), (60, 70), (60, 40), (70, 40)], fill=COLOR_ACTIVE)

def draw_search(draw):
    draw.ellipse([15, 15, 55, 55], outline=COLOR_NORMAL, width=4)
    draw.line([(45, 45), (70, 70)], fill=COLOR_NORMAL, width=4)

def draw_search_active(draw):
    draw.ellipse([15, 15, 55, 55], outline=COLOR_ACTIVE, width=4)
    draw.line([(45, 45), (70, 70)], fill=COLOR_ACTIVE, width=4)

def draw_create(draw):
    draw.rectangle([20, 20, 60, 60], outline=COLOR_NORMAL, width=4)
    draw.line([(40, 25), (40, 55)], fill=COLOR_NORMAL, width=4)
    draw.line([(25, 40), (55, 40)], fill=COLOR_NORMAL, width=4)

def draw_create_active(draw):
    draw.rectangle([20, 20, 60, 60], outline=COLOR_ACTIVE, width=4)
    draw.line([(40, 25), (40, 55)], fill=COLOR_ACTIVE, width=4)
    draw.line([(25, 40), (55, 40)], fill=COLOR_ACTIVE, width=4)

def draw_ai(draw):
    draw.rounded_rectangle([15, 25, 65, 60], radius=8, outline=COLOR_NORMAL, width=4)
    draw.polygon([(25, 60), (30, 70), (35, 60)], fill=COLOR_NORMAL)

def draw_ai_active(draw):
    draw.rounded_rectangle([15, 25, 65, 60], radius=8, outline=COLOR_ACTIVE, width=4)
    draw.polygon([(25, 60), (30, 70), (35, 60)], fill=COLOR_ACTIVE)

def draw_profile(draw):
    draw.ellipse([25, 10, 55, 40], fill=COLOR_NORMAL)
    draw.arc([10, 45, 70, 80], 0, 180, fill=COLOR_NORMAL, width=6)

def draw_profile_active(draw):
    draw.ellipse([25, 10, 55, 40], fill=COLOR_ACTIVE)
    draw.arc([10, 45, 70, 80], 0, 180, fill=COLOR_ACTIVE, width=6)

icons = [
    ('home.png', draw_home),
    ('home-active.png', draw_home_active),
    ('search.png', draw_search),
    ('search-active.png', draw_search_active),
    ('create.png', draw_create),
    ('create-active.png', draw_create_active),
    ('ai.png', draw_ai),
    ('ai-active.png', draw_ai_active),
    ('profile.png', draw_profile),
    ('profile-active.png', draw_profile_active),
]

for filename, draw_func in icons:
    draw_icon(filename, draw_func)
    print(f'Generated {filename}')

print('All icons generated successfully!')
