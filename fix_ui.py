import os
import glob

def fix_file(filepath):
    with open(filepath, 'r') as f:
        content = f.read()

    # Cards and panels must use larger radii (rounded-3xl)
    content = content.replace('bg-surface-container-low p-6 rounded-xl', 'bg-surface-container-low p-6 rounded-3xl')
    content = content.replace('bg-surface-container-low p-8 rounded-xl', 'bg-surface-container-low p-8 rounded-3xl')
    content = content.replace('bg-surface-container-low glow-hover p-8 rounded-xl', 'bg-surface-container-low glow-hover p-8 rounded-3xl')
    content = content.replace('bg-surface-container-highest glow-hover p-8 rounded-xl', 'bg-surface-container-highest glow-hover p-8 rounded-3xl')
    content = content.replace('bg-surface-container-low rounded-xl overflow-x-auto', 'bg-surface-container-low rounded-3xl overflow-x-auto')
    content = content.replace('glass-panel rounded-xl p-8', 'glass-panel rounded-3xl p-8')
    content = content.replace('bg-surface-container-low rounded-xl p-8', 'bg-surface-container-low rounded-3xl p-8')
    content = content.replace('bg-surface-container-high rounded-xl p-4', 'bg-surface-container-high rounded-3xl p-4')
    content = content.replace('bg-surface-container-lowest border border-outline-variant/20 rounded-xl', 'bg-surface-container-lowest border border-outline-variant/20 rounded-3xl')
    content = content.replace('glass-panel p-8 rounded-xl', 'glass-panel p-8 rounded-3xl')
    content = content.replace('bg-surface-container-highest rounded-xl', 'bg-surface-container-highest rounded-3xl')
    content = content.replace('bg-surface-container-low rounded-xl', 'bg-surface-container-low rounded-3xl')
    content = content.replace('glass-panel p-10 rounded-xl', 'glass-panel p-10 rounded-3xl')
    content = content.replace('bg-surface-container-highest p-10 rounded-xl', 'bg-surface-container-highest p-10 rounded-3xl')
    content = content.replace('bg-surface-container-highest p-6 rounded-xl', 'bg-surface-container-highest p-6 rounded-3xl')
    content = content.replace('bg-surface-container-low p-8 rounded-xl', 'bg-surface-container-low p-8 rounded-3xl')
    content = content.replace('bg-surface-container-lowest rounded-xl', 'bg-surface-container-lowest rounded-3xl')
    content = content.replace('glass-panel rounded-xl', 'glass-panel rounded-3xl')
    content = content.replace('bg-surface-container-low rounded-xl', 'bg-surface-container-low rounded-3xl')
    content = content.replace('bg-surface-container-highest rounded-xl', 'bg-surface-container-highest rounded-3xl')
    content = content.replace('bg-surface-container-low/40 neo-blur p-6 rounded-xl', 'bg-surface-container-low/40 neo-blur p-6 rounded-3xl')

    # glass-panel rounded-lg in pricing
    content = content.replace('glass-panel rounded-lg', 'glass-panel rounded-3xl')

    if filepath.endswith('waitlist.astro'):
        # Fix focus ring on waitlist form input
        content = content.replace('bg-transparent border-none focus:ring-0 text-on-surface', 'bg-transparent border-none text-on-surface')
        # Fix waitlist form focus classes
        content = content.replace('focus-within:ring-1 focus-within:ring-primary focus-within:border-primary', 'focus-within:ring-1 focus-within:ring-primary focus-within:ring-inset')
        # Remove neon button glow
        content = content.replace('hover:shadow-[0_0_50px_rgba(186,158,255,0.4)]', '')

    with open(filepath, 'w') as f:
        f.write(content)

for root, _, files in os.walk('website/src'):
    for file in files:
        if file.endswith('.astro'):
            fix_file(os.path.join(root, file))
